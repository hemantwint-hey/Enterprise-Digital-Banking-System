param(
    [ValidateSet("Health", "Info", "Transactions")]
    [string]$Target = "Info",
    [int]$TotalRequests = 3000,
    [int]$Concurrency = 100
)

# Compile native high-performance C# benchmark engine
Add-Type -ReferencedAssemblies "System.Net.Http" -TypeDefinition @"
using System;
using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;

public class TurboBenchmarkEngine {
    public static string Execute(string url, int totalRequests, int concurrency, string token) {
        ServicePointManager.DefaultConnectionLimit = 2000;
        ServicePointManager.Expect100Continue = false;
        ServicePointManager.UseNagleAlgorithm = false;

        var handler = new HttpClientHandler {
            MaxConnectionsPerServer = 2000
        };
        var client = new HttpClient(handler);
        client.Timeout = TimeSpan.FromSeconds(15);
        if (!string.IsNullOrEmpty(token)) {
            client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);
        }

        int successCount = 0;
        int failureCount = 0;
        var sw = Stopwatch.StartNew();

        var semaphore = new SemaphoreSlim(concurrency);
        var tasks = new Task[totalRequests];

        for (int i = 0; i < totalRequests; i++) {
            tasks[i] = Task.Run(async () => {
                await semaphore.WaitAsync();
                try {
                    using (var resp = await client.GetAsync(url)) {
                        if (resp.IsSuccessStatusCode) Interlocked.Increment(ref successCount);
                        else Interlocked.Increment(ref failureCount);
                    }
                } catch {
                    Interlocked.Increment(ref failureCount);
                } finally {
                    semaphore.Release();
                }
            });
        }

        Task.WaitAll(tasks);
        sw.Stop();

        double sec = Math.Max(sw.Elapsed.TotalSeconds, 0.001);
        double rps = Math.Round(totalRequests / sec, 2);
        double avgLatency = Math.Round(sw.ElapsedMilliseconds / (double)totalRequests, 2);

        Console.WriteLine("\n============================================================");
        Console.WriteLine(">>> 🚀 TURBO BENCHMARK REPORT (JAVA 21 VIRTUAL THREADS) <<<");
        Console.WriteLine("============================================================");
        Console.WriteLine(string.Format("Target Endpoint     : {0}", url));
        Console.WriteLine(string.Format("Total Requests Sent : {0}", totalRequests));
        Console.WriteLine(string.Format("Successful (200 OK) : {0}", successCount));
        Console.WriteLine(string.Format("Failed Requests     : {0}", failureCount));
        Console.WriteLine(string.Format("Total Time Taken    : {0} seconds ({1} ms)", Math.Round(sec, 2), sw.ElapsedMilliseconds));
        Console.WriteLine(string.Format("Throughput (RPS)    : {0} requests/sec  <<<", rps));
        Console.WriteLine(string.Format("Average Latency     : {0} ms / request", avgLatency));
        Console.WriteLine("============================================================\n");
        return rps.ToString();
    }
}
"@ -ErrorAction SilentlyContinue

$url = "http://localhost:8080/actuator/info"
$token = ""

if ($Target -eq "Health") {
    $url = "http://localhost:8080/actuator/health"
} elseif ($Target -eq "Transactions") {
    $url = "http://localhost:8080/api/v1/transactions/account/1/history"
    Write-Host "Authenticating for Transactions endpoint..." -ForegroundColor Gray
    try {
        $loginBody = @{ email = "loadtest_live@bank.com"; password = "Password@123" } | ConvertTo-Json
        $loginResp = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -ContentType "application/json" -Body $loginBody
        $token = $loginResp.token
    } catch {}
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Launching Turbo Benchmark with Concurrency: $Concurrency" -ForegroundColor Cyan
Write-Host "Target URL: $url" -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Cyan

[TurboBenchmarkEngine]::Execute($url, $TotalRequests, $Concurrency, $token)

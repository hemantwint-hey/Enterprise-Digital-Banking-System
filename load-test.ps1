param(
    [ValidateSet("Read", "Write", "Both", "Turbo")]
    [string]$Mode = "Both",
    [int]$TotalRequests = 10000,
    [int]$Concurrency = 200,
    [int]$AccountId = 1,
    [int]$AccountRange = 1
)

# =============================================================================
# Turbo High-Concurrency Benchmark Engine V2 (C# / .NET)
# Eliminates Windows PowerShell client-side thread-pool bottlenecks.
# Tracks live progress, throughput, and p50 / p95 / p99 latency percentiles.
# =============================================================================
if (-not ([System.Management.Automation.PSTypeName]'TurboBenchmarkEngineV2').Type) {
    Add-Type -ReferencedAssemblies "System.Net.Http" -TypeDefinition @"
using System;
using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

public class TurboBenchmarkEngineV2 {
    public static void Execute(string mode, int totalRequests, int concurrency, int accountId, int accountRange, string token) {
        ServicePointManager.DefaultConnectionLimit = 5000;
        ServicePointManager.Expect100Continue = false;
        ServicePointManager.UseNagleAlgorithm = false;

        var handler = new HttpClientHandler {
            MaxConnectionsPerServer = 5000
        };
        var client = new HttpClient(handler);
        client.Timeout = TimeSpan.FromSeconds(30);

        if (!string.IsNullOrEmpty(token)) {
            client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", token);
        }

        string writeUrl = "http://localhost:8080/api/v1/transactions/deposit";
        string turboUrl = "http://localhost:8080/actuator/info";

        int successReads = 0;
        int failedReads = 0;
        int successWrites = 0;
        int failedWrites = 0;
        int completedCount = 0;

        long[] latenciesMs = new long[totalRequests];

        var sw = Stopwatch.StartNew();
        var semaphore = new SemaphoreSlim(concurrency);
        var tasks = new Task[totalRequests];

        int progressStep = Math.Max(1000, totalRequests / 10);
        Console.WriteLine("\n[Load Engine] Firing " + totalRequests.ToString("N0") + " requests across " + concurrency + " concurrent slots...");
        Console.WriteLine("[Load Engine] Real-time progress updates active:\n");

        for (int i = 0; i < totalRequests; i++) {
            int reqIndex = i;
            tasks[i] = Task.Run(async () => {
                await semaphore.WaitAsync();
                var reqSw = Stopwatch.StartNew();
                try {
                    bool isWrite = false;
                    if (mode == "Write") isWrite = true;
                    else if (mode == "Both") isWrite = (reqIndex % 2 == 1);
                    else if (mode == "Turbo") isWrite = false;

                    int targetAcc = accountId;
                    if (accountRange > 1) {
                        targetAcc = accountId + (reqIndex % accountRange);
                    }

                    if (mode == "Turbo") {
                        using (var resp = await client.GetAsync(turboUrl)) {
                            if (resp.IsSuccessStatusCode) Interlocked.Increment(ref successReads);
                            else Interlocked.Increment(ref failedReads);
                        }
                    } else if (!isWrite) {
                        string readUrl = string.Format("http://localhost:8080/api/v1/transactions/account/{0}/history", targetAcc);
                        using (var resp = await client.GetAsync(readUrl)) {
                            if (resp.IsSuccessStatusCode) Interlocked.Increment(ref successReads);
                            else Interlocked.Increment(ref failedReads);
                        }
                    } else {
                        string depositJson = string.Format(
                            "{{\"accountId\":{0},\"amount\":10.00,\"remarks\":\"LoadTest_Tx_{1}\"}}",
                            targetAcc, reqIndex
                        );
                        using (var content = new StringContent(depositJson, Encoding.UTF8, "application/json")) {
                            using (var resp = await client.PostAsync(writeUrl, content)) {
                                if (resp.IsSuccessStatusCode) Interlocked.Increment(ref successWrites);
                                else Interlocked.Increment(ref failedWrites);
                            }
                        }
                    }
                } catch {
                    if (mode == "Write") Interlocked.Increment(ref failedWrites);
                    else Interlocked.Increment(ref failedReads);
                } finally {
                    reqSw.Stop();
                    latenciesMs[reqIndex] = reqSw.ElapsedMilliseconds;
                    semaphore.Release();
                    int done = Interlocked.Increment(ref completedCount);
                    if (done % progressStep == 0 || done == totalRequests) {
                        double elapsedSec = Math.Max(sw.Elapsed.TotalSeconds, 0.001);
                        double currentRps = done / elapsedSec;
                        double pct = (done / (double)totalRequests) * 100.0;
                        Console.WriteLine(string.Format("  --> [{0,5:F1}%] Completed: {1,6:N0} / {2,6:N0} reqs | Instant Throughput: {3,7:F1} RPS | Elapsed: {4:F1}s", 
                            pct, done, totalRequests, currentRps, elapsedSec));
                    }
                }
            });
        }

        Task.WaitAll(tasks);
        sw.Stop();

        Array.Sort(latenciesMs);
        long minLatency = latenciesMs.Length > 0 ? latenciesMs[0] : 0;
        long maxLatency = latenciesMs.Length > 0 ? latenciesMs[latenciesMs.Length - 1] : 0;
        long p50 = latenciesMs.Length > 0 ? latenciesMs[(int)(latenciesMs.Length * 0.50)] : 0;
        long p95 = latenciesMs.Length > 0 ? latenciesMs[(int)(latenciesMs.Length * 0.95)] : 0;
        long p99 = latenciesMs.Length > 0 ? latenciesMs[(int)(latenciesMs.Length * 0.99)] : 0;

        int totalSuccess = successReads + successWrites;
        int totalFailed = failedReads + failedWrites;
        double sec = Math.Max(sw.Elapsed.TotalSeconds, 0.001);
        double rps = Math.Round(totalRequests / sec, 2);
        double avgLatency = Math.Round(sw.ElapsedMilliseconds / (double)totalRequests, 2);

        Console.WriteLine("\n============================================================");
        Console.WriteLine(">>> 🚀 ENTERPRISE BANKING BENCHMARK REPORT (JAVA 21) <<<");
        Console.WriteLine("============================================================");
        Console.WriteLine(string.Format("Test Mode           : {0}", mode));
        Console.WriteLine(string.Format("Concurrency (Slots) : {0}", concurrency));
        Console.WriteLine(string.Format("Total Requests Sent : {0:N0}", totalRequests));
        if (mode == "Both" || mode == "Read" || mode == "Turbo") {
            Console.WriteLine(string.Format("  |-- Read Requests : {0:N0} (Success: {1:N0}, Fail: {2:N0})", successReads + failedReads, successReads, failedReads));
        }
        if (mode == "Both" || mode == "Write") {
            Console.WriteLine(string.Format("  \\-- Write Requests: {0:N0} (Success: {1:N0}, Fail: {2:N0})", successWrites + failedWrites, successWrites, failedWrites));
        }
        Console.WriteLine(string.Format("Total Successful    : {0:N0} (200 OK)", totalSuccess));
        Console.WriteLine(string.Format("Total Failed        : {0:N0}", totalFailed));
        Console.WriteLine(string.Format("Total Time Taken    : {0:F2} seconds ({1:N0} ms)", sec, sw.ElapsedMilliseconds));
        Console.WriteLine(string.Format("Throughput (RPS)    : {0:N2} requests/sec  <<< 🔥", rps));
        Console.WriteLine("------------------------------------------------------------");
        Console.WriteLine("Latency Distribution (Percentiles):");
        Console.WriteLine(string.Format("  |-- Min Latency   : {0} ms", minLatency));
        Console.WriteLine(string.Format("  |-- Mean Latency  : {0:F2} ms", avgLatency));
        Console.WriteLine(string.Format("  |-- p50 (Median)  : {0} ms", p50));
        Console.WriteLine(string.Format("  |-- p95 Latency   : {0} ms", p95));
        Console.WriteLine(string.Format("  \\-- p99 Latency   : {0} ms (Tail Latency)", p99));
        Console.WriteLine("============================================================");
        Console.WriteLine("👉 Check your Grafana Dashboard (http://localhost:3000) now!");
        Console.WriteLine("============================================================\n");
    }
}
"@
}

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host ">>> ENTERPRISE DIGITAL BANKING LOAD TEST BENCHMARK <<<" -ForegroundColor Green
Write-Host "Mode            : $Mode (Read / Write / Both / Turbo)" -ForegroundColor Yellow
Write-Host "Total Requests  : $TotalRequests" -ForegroundColor Yellow
Write-Host "Concurrency     : $Concurrency" -ForegroundColor Yellow
Write-Host "Target Account  : $AccountId" -ForegroundColor Yellow
if ($AccountRange -gt 1) {
    Write-Host "Account Range   : $AccountId to $($AccountId + $AccountRange - 1)" -ForegroundColor Yellow
}
Write-Host "============================================================" -ForegroundColor Cyan

$token = ""
if ($Mode -ne "Turbo") {
    Write-Host "Authenticating test user for Banking APIs..." -ForegroundColor Gray
    try {
        $loginBody = @{ email = "loadtest_live@bank.com"; password = "Password@123" } | ConvertTo-Json
        $loginResp = Invoke-RestMethod -Uri "http://localhost:8080/auth/login" -Method Post -ContentType "application/json" -Body $loginBody -TimeoutSec 5
        $token = $loginResp.token
        Write-Host "[OK] JWT Authentication Successful!" -ForegroundColor Green
    } catch {
        Write-Host "[WARN] Could not authenticate. Running in unauthenticated mode or check if app is running." -ForegroundColor Yellow
    }
}

[TurboBenchmarkEngineV2]::Execute($Mode, $TotalRequests, $Concurrency, $AccountId, $AccountRange, $token)

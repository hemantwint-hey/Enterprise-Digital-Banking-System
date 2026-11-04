Add-Type -ReferencedAssemblies "System.Net.Http" -TypeDefinition @"
using System;
using System.Diagnostics;
using System.Net;
using System.Net.Http;
using System.Threading;
using System.Threading.Tasks;

public class TurboBenchmark {
    public static string Run(string url, int totalRequests, int concurrency, string token) {
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

        return string.Format("Total: {0} | Success: {1} | Failed: {2} | Time: {3}s | RPS: {4} requests/sec | Latency: {5}ms",
            totalRequests, successCount, failureCount, Math.Round(sec, 2), rps, avgLatency);
    }
}
"@

$result = [TurboBenchmark]::Run("http://localhost:8080/actuator/info", 3000, 100, "")
Write-Host $result -ForegroundColor Green

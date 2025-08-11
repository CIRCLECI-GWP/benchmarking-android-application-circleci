const benchmarkData = require('/home/circleci/benchmarks/com.example.macrobenchmark-benchmarkData.json')

const STARTUP_MEDIAN_THRESHOLD_MILIS = 400

// Handle different possible metric names
function getStartupMetrics(benchmark) {
    // Check for different possible startup metric names
    if (benchmark.metrics.startupMs) {
        return benchmark.metrics.startupMs;
    } else if (benchmark.metrics.timeToInitialDisplayMs) {
        return benchmark.metrics.timeToInitialDisplayMs;
    } else if (benchmark.metrics.timeToFullDisplayMs) {
        return benchmark.metrics.timeToFullDisplayMs;
    }
    return null;
}

let err = 0;

// Process all startup-related benchmarks
benchmarkData.benchmarks.forEach((benchmark, index) => {
    const metrics = getStartupMetrics(benchmark);
    
    if (!metrics) {
        console.log(`⚠️  Benchmark ${index + 1} (${benchmark.name || 'unnamed'}) - No startup metrics found, skipping`);
        return;
    }

    const mode = benchmark.params.mode || 'SINGLE';
    const benchmarkName = benchmark.name || `benchmark_${index + 1}`;
    const medianTime = metrics.median;
    
    let msg = `${mode} startup metrics (${benchmarkName}) - ${medianTime}ms `;
    
    if (medianTime > STARTUP_MEDIAN_THRESHOLD_MILIS) {
        err = 1;
        console.error(`${msg} ❌ - OVER THRESHOLD ${STARTUP_MEDIAN_THRESHOLD_MILIS}ms`);
    } else {
        console.log(`${msg} ✅`);
    }
});

// If no benchmarks were processed, that's an error
if (benchmarkData.benchmarks.length === 0) {
    console.error('❌ No benchmarks found in the data file');
    err = 1;
}

process.exit(err)
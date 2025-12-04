"use client";
import { useState, useEffect, useCallback } from "react";
import { Zap, HeartHandshake, XCircle, BarChart3 } from "lucide-react";

// --- Type Definitions ---
type Status = "healthy" | "unhealthy";

interface ApiStatus {
  status: Status;
  message: string;
  port: number;
}

interface MarketStats {
  averagePrice: number;
  medianPrice: number;
  totalVolume: number;
  priceChangePercent: number;
  averageSquareFootage: number;
  oldestYear: number;
  newestYear: number;
}

// --- StatusPill Component ---
const StatusPill: React.FC<ApiStatus> = ({ status, message, port }) => {
  const color = status === "healthy" ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700";
  const Icon = status === "healthy" ? HeartHandshake : XCircle;

  return (
    <div className={`px-4 py-2 rounded-full text-sm font-medium flex items-center gap-2 ${color}`}>
      <Icon className="w-4 h-4" />
      <span>{message} (Port: {port})</span>
    </div>
  );
};

// --- Main Application Component ---
export default function Home() {
  const [pythonApiStatus, setPythonApiStatus] = useState<ApiStatus>({ 
    status: "unhealthy", 
    message: "Loading...", 
    port: 8000 
  });
  const [javaApiStatus, setJavaApiStatus] = useState<ApiStatus>({ 
    status: "unhealthy", 
    message: "Loading...", 
    port: 8080 
  });
  const [marketStats, setMarketStats] = useState<MarketStats | null>(null);
  const [error, setError] = useState<string | null>(null);

  const PYTHON_API_URL = typeof window !== 'undefined' 
    ? "http://localhost:8000"
    : (process.env.NEXT_PUBLIC_ML_API_URL || "http://ml-api:8000");
    
  const JAVA_API_URL = typeof window !== 'undefined'
    ? "http://localhost:8080"
    : (process.env.NEXT_PUBLIC_JAVA_API_URL || "http://java-api:8080");

  // Check Python API status
  const checkPythonApiStatus = useCallback(async () => {
    try {
      const response = await fetch(`${PYTHON_API_URL}/health`);
      if (response.ok) {
        setPythonApiStatus({ status: "healthy", message: "ML API Healthy", port: 8000 });
      } else {
        setPythonApiStatus({ status: "unhealthy", message: "ML API Not Ready", port: 8000 });
      }
    } catch (e) {
      setPythonApiStatus({ status: "unhealthy", message: "ML API Disconnected", port: 8000 });
    }
  }, [PYTHON_API_URL]);

  // Check Java API status
  const checkJavaApiStatus = useCallback(async () => {
    try {
      const response = await fetch(`${JAVA_API_URL}/api/health`);
      if (response.ok) {
        const data = await response.json();
        setJavaApiStatus({ 
          status: "healthy", 
          message: `Java API: ${data.data?.status || 'UP'}`, 
          port: 8080 
        });
      } else {
        setJavaApiStatus({ status: "unhealthy", message: "Java API Not Ready", port: 8080 });
      }
    } catch (e) {
      setJavaApiStatus({ status: "unhealthy", message: "Java API Disconnected", port: 8080 });
    }
  }, [JAVA_API_URL]);

  const fetchMarketStats = useCallback(async () => {
    try {
      setError(null);
      const response = await fetch(`${JAVA_API_URL}/api/market-analysis/stats`);
      if (response.ok) {
        const apiResponse = await response.json();
        setMarketStats(apiResponse.data);
      } else {
        const errorText = await response.text();
        setError(`Java API Error (${response.status}): ${errorText.substring(0, 100)}...`);
        setMarketStats(null);
      }
    } catch (e) {
      setError(`Connection failed: ${e instanceof Error ? e.message : 'Unknown error'}`);
      setMarketStats(null);
    }
  }, [JAVA_API_URL]);

  // Periodic API status and data checks
  useEffect(() => {
    checkPythonApiStatus();
    checkJavaApiStatus();
    fetchMarketStats();

    const pythonInterval = setInterval(checkPythonApiStatus, 5000);
    const javaInterval = setInterval(checkJavaApiStatus, 5000);
    const statsInterval = setInterval(fetchMarketStats, 10000);

    return () => {
      clearInterval(pythonInterval);
      clearInterval(javaInterval);
      clearInterval(statsInterval);
    };
  }, [checkPythonApiStatus, checkJavaApiStatus, fetchMarketStats]);

  return (
    <div className="min-h-screen bg-gray-50 p-8 md:p-12 lg:p-16">
      <header className="max-w-7xl mx-auto mb-10">
        <h1 className="text-6xl font-extrabold text-gray-800 mb-4 tracking-tight">
          HSBC Real Estate AI Platform
        </h1>
        <p className="text-xl text-gray-600">Property valuation system combining machine learning with real-time data</p>
      </header>

      <main className="max-w-7xl mx-auto">
        {/* API Status & Market Data Card */}
        <div className="bg-white p-8 shadow-2xl rounded-3xl space-y-8">
          {/* API Status Section */}
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-3">
              <Zap className="text-yellow-500 w-8 h-8" />
              API Service Status
            </h2>
            <div className="flex flex-wrap gap-4">
              <StatusPill {...pythonApiStatus} />
              <StatusPill {...javaApiStatus} />
            </div>
          </div>

          {/* Market Data Section */}
          <div>
            <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-3">
              <BarChart3 className="text-blue-500 w-8 h-8" />
              Market Data Overview
            </h2>

            {error && (
              <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 mb-4 rounded-lg">
                <p className="font-bold">Data Loading Error</p>
                <p className="text-sm">{error}</p>
              </div>
            )}

            {marketStats ? (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <div className="p-6 bg-red-50 rounded-xl">
                  <p className="text-sm font-semibold text-red-600 mb-2">Total Properties</p>
                  <p className="text-4xl font-extrabold text-red-800">
                    {marketStats.totalVolume.toLocaleString()}
                  </p>
                </div>

                <div className="p-6 bg-gray-50 rounded-xl">
                  <p className="text-sm font-semibold text-gray-600 mb-2">Average Price</p>
                  <p className="text-4xl font-extrabold text-gray-800">
                    ${Math.round(marketStats.averagePrice).toLocaleString()}
                  </p>
                </div>

                <div className="p-6 bg-blue-50 rounded-xl">
                  <p className="text-sm font-semibold text-blue-600 mb-2">Median Price</p>
                  <p className="text-4xl font-extrabold text-blue-800">
                    ${Math.round(marketStats.medianPrice).toLocaleString()}
                  </p>
                </div>

                <div className="p-6 bg-green-50 rounded-xl">
                  <p className="text-sm font-semibold text-green-600 mb-2">Price Change</p>
                  <p className="text-4xl font-extrabold text-green-800">
                    {marketStats.priceChangePercent > 0 ? '+' : ''}
                    {marketStats.priceChangePercent.toFixed(2)}%
                  </p>
                </div>

                <div className="md:col-span-2 lg:col-span-4 pt-6 border-t">
                  <div className="flex flex-wrap gap-8">
                    <div>
                      <p className="text-sm text-gray-600 font-semibold mb-1">Average Area</p>
                      <p className="text-2xl font-bold text-gray-800">
                        {Math.round(marketStats.averageSquareFootage).toLocaleString()} sq ft
                      </p>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600 font-semibold mb-1">Construction Period</p>
                      <p className="text-2xl font-bold text-gray-800">
                        {marketStats.oldestYear} - {marketStats.newestYear}
                      </p>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="text-center py-12">
                <div className="animate-pulse">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mx-auto mb-4"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/2 mx-auto"></div>
                </div>
                <p className="text-gray-500 mt-4">Loading market data from Java API...</p>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}

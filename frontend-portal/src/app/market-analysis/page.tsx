"use client";
import { useState, useMemo } from "react";
import { TrendingUp, Loader2 } from "lucide-react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";

// --- API Configurations ---
// Market Data URL is no longer needed but kept for context, though fetchProperties is removed.
const JAVA_API_URL = "http://localhost:8080"; 
// This remains the key API endpoint for the What-If tool
const WHATIF_PROXY_API = "/api/properties/what-if"; 

// --- What-if Schema (Matches App 1 for consistency) ---
const whatIfSchema = z.object({
  square_footage: z.coerce.number().min(100, "Must be at least 100 sq ft"),
  bedrooms: z.coerce.number().int().min(1, "Min 1 bedroom").max(10, "Max 10 bedrooms"),
  bathrooms: z.coerce.number().min(1, "Min 1 bathroom").max(10, "Max 10 bathrooms"),
  year_built: z.coerce.number().min(1900, "Min 1900").max(new Date().getFullYear(), `Max ${new Date().getFullYear()}`),
  lot_size: z.coerce.number().min(1000, "Must be at least 1000 sq ft"),
  distance_to_city_center: z.coerce.number().min(1, "Min 1").max(10, "Max 10"),
  school_rating: z.coerce.number().min(1, "Min 1").max(10, "Max 10"),
});

type WhatIfFormData = z.infer<typeof whatIfSchema>;

// --- Utility Functions ---
// Function to handle exponential backoff for API calls
const fetchWithRetry = async (url: string, options: RequestInit, retries = 3, delay = 1000): Promise<Response> => {
  try {
    const response = await fetch(url, options);
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response;
  } catch (error) {
    if (retries > 0) {
      // console.log(`Attempt failed. Retrying in ${delay / 1000}s...`);
      await new Promise(resolve => setTimeout(resolve, delay));
      return fetchWithRetry(url, options, retries - 1, delay * 2);
    }
    throw error;
  }
};

// --- Main Component ---
export default function MarketAnalysisPage() {
  // Removed all state related to market data (properties, yearlyStats, loading, error, filters)
  // Only What-if Tool States remain
  const [whatIfResult, setWhatIfResult] = useState<number | null>(null);
  const [whatIfLoading, setWhatIfLoading] = useState(false);
  const [whatIfError, setWhatIfError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors: whatIfErrors },
  } = useForm<WhatIfFormData>({
    resolver: zodResolver(whatIfSchema),
    defaultValues: {
      square_footage: 1850,
      bedrooms: 3,
      bathrooms: 2,
      year_built: 2005,
      lot_size: 8200,
      distance_to_city_center: 4.8,
      school_rating: 8.7,
    },
  });

  // --- What-if Submission Logic (Calling Java Proxy) ---
  const onWhatIfSubmit = async (data: WhatIfFormData) => {
    setWhatIfLoading(true);
    setWhatIfError(null);
    setWhatIfResult(null);

    // Payload creation
    const payload = {
        // Integer (snake_case from @JsonProperty)
        square_footage: Math.round(Number(data.square_footage)),
        year_built: Math.round(Number(data.year_built)),
        lot_size: Math.round(Number(data.lot_size)),
        
        // Integer (camelCase, 
        bedrooms: Math.round(Number(data.bedrooms)),
        
        // Double (camelCase/snake_case)
        bathrooms: Number(data.bathrooms),
        distance_to_city_center: Number(data.distance_to_city_center),
        school_rating: Number(data.school_rating),
    };
    // --- End payload creation ---

    try {
      // 
      const response = await fetchWithRetry(WHATIF_PROXY_API, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      const apiResponse = await response.json();

      // 
      if (response.ok && apiResponse.data?.predicted_price !== undefined) {
        // 
        setWhatIfResult(apiResponse.data.predicted_price);
      } else {
        // 
        const validationErrors = apiResponse.error || apiResponse.message || "Unknown validation error.";
        setWhatIfError(`Prediction failed: ${validationErrors}`);
      }
    } catch (err) {
      console.error("What-if API Error:", err);
      setWhatIfError("Failed to connect to the Java proxy service. Check Java backend.");
    } finally {
      // Note: Setting whatIfLoading to false here, not the global 'loading' state which was removed.
      setWhatIfLoading(false); 
    }
  };


  // --- UI Components ---
  const InputField = ({ name, label, type = "number", errors, isRequired = true }: { 
    name: keyof WhatIfFormData, 
    label: string, 
    type?: string, 
    errors: any, 
    isRequired?: boolean 
  }) => (
    <div className="flex flex-col">
      <label htmlFor={name} className="text-sm font-medium text-gray-700 mb-1">
        {label} {isRequired && <span className="text-red-500">*</span>}
      </label>
      <input
        id={name}
        type={type}
        step={name === 'bathrooms' || name === 'school_rating' || name === 'distance_to_city_center' ? "any" : "1"}
        placeholder={`Enter ${label.toLowerCase()}`}
        {...register(name)}
        className={`w-full px-3 py-2 border rounded-lg text-sm transition focus:ring-red-500 focus:border-red-500 ${errors[name] ? 'border-red-500 bg-red-50' : 'border-gray-300'}`}
      />
      {errors[name] && <p className="mt-1 text-xs text-red-500">{errors[name]?.message}</p>}
    </div>
  );
  
  // Since all market data logic (fetchProperties, calculateYearlyStats, useEffect, interfaces) is gone, 
  // we can also remove the unnecessary imports and constants related to charting/filtering/data.

  return (
    <div className="p-4 md:p-8 bg-gray-50 min-h-screen">
      <header className="mb-8">
        <h1 className="text-4xl font-extrabold text-gray-900 border-b pb-2">
          Property Market Analysis Dashboard
        </h1>
        <p className="text-lg text-gray-600 mt-2">
          Interactive insights into property market trends (Java Backend Simulation).
        </p>
      </header>
      
      {/* 僅保留 What-if Analysis Tool，讓它佔滿整個內容區塊 */}
      <div className="grid grid-cols-1 gap-8 mb-8">
        
        {/* --- What-if Analysis Tool --- */}
        <div className="p-6 bg-white rounded-xl shadow-lg">
          <h2 className="text-xl font-bold text-blue-600 mb-4 flex items-center">
              <TrendingUp className="w-5 h-5 mr-2"/> What-If Analysis Tool
          </h2>
          <p className="text-gray-600 mb-4 text-sm">
            Simulate the predicted value of a custom property configuration using the core ML model.
          </p>
          <form onSubmit={handleSubmit(onWhatIfSubmit)} className="space-y-4">
            <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
              <InputField name="square_footage" label="Sq Footage" errors={whatIfErrors} />
              <InputField name="bedrooms" label="Bedrooms" errors={whatIfErrors} />
              <InputField name="bathrooms" label="Bathrooms" errors={whatIfErrors} />
              <InputField name="year_built" label="Year Built" errors={whatIfErrors} />
              <InputField name="lot_size" label="Lot Size" errors={whatIfErrors} />
              <InputField name="distance_to_city_center" label="Distance to City Center" errors={whatIfErrors} />
              <InputField name="school_rating" label="School Rating" errors={whatIfErrors} />
              <div className="flex items-end pt-2">
                <button
                    type="submit"
                    disabled={whatIfLoading}
                    className="w-full py-2 bg-blue-500 text-white font-semibold rounded-lg hover:bg-blue-600 transition disabled:opacity-50 flex items-center justify-center gap-2"
                >
                    {whatIfLoading ? <Loader2 className="w-5 h-5 animate-spin" /> : 'Predict'}
                </button>
              </div>
            </div>
          </form>

          {whatIfError && (
              <div className="mt-4 text-center bg-red-100 border-l-4 border-red-500 text-red-700 p-3 rounded-lg text-sm" role="alert">
                  <p>{whatIfError}</p>
              </div>
          )}

          {whatIfResult !== null && (
              <div className="mt-6 text-center bg-blue-50 rounded-lg p-6 border border-blue-200">
                  <p className="text-xl text-blue-600 font-semibold mb-2">Predicted Value:</p>
                  <p className="text-4xl font-black text-blue-700">
                      ${whatIfResult.toLocaleString()}
                  </p>
              </div>
          )}
        </div>
      </div>
      
      {/* 由於所有數據相關的區塊都被移除，這裡只留下 What-If Tool */}
      
    </div>
  );
}

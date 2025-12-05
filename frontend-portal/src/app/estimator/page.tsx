"use client";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import * as z from "zod";
import { Loader2 } from "lucide-react";
// *** 新增 Recharts 引用 ***
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
// *************************

// The ML API URL is used directly since it's a microservice in the same environment.
const API_URL = "http://localhost:8000/predict";

// 1. Define the Schema for input validation
const schema = z.object({
  square_footage: z.coerce.number().min(100, "Must be at least 100 sq ft"),
  bedrooms: z.coerce.number().int().min(1, "Min 1 bedroom").max(10, "Max 10 bedrooms"),
  bathrooms: z.coerce.number().min(1, "Min 1 bathroom").max(10, "Max 10 bathrooms"),
  year_built: z.coerce.number().min(1900, "Min 1900").max(new Date().getFullYear(), `Max ${new Date().getFullYear()}`),
  lot_size: z.coerce.number().min(1000, "Must be at least 1000 sq ft"),
  distance_to_city_center: z.coerce.number().min(1, "Min 1").max(10, "Max 10"),
  school_rating: z.coerce.number().min(1, "Min 1").max(10, "Max 10"),
});

type FormData = z.infer<typeof schema>;

interface PredictionHistory extends FormData {
    id: number;
    predictedPrice: number;
    timestamp: number;
}


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

const formatFeatureName = (name: string): string => {
    const formatted = name.replace(/([A-Z])/g, ' $1').toLowerCase();
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
}

// 2. Main Estimator Component
export default function Estimator() {
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [history, setHistory] = useState<PredictionHistory[]>([]);
  
  const MARKET_TRAINING_MEAN_PRICE = 270375; 

  const {
    register,
    handleSubmit,
    formState: { errors },
    getValues,
  } = useForm<FormData>({
    resolver: zodResolver(schema),
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

  const onSubmit = async (data: FormData) => {
    setLoading(true);
    setResult(null);
    setError(null);

    const payload = {
      ...data,
      // Ensure numeric types are used where required by the API
      square_footage: Number(data.square_footage),
      bedrooms: Number(data.bedrooms),
      bathrooms: Number(data.bathrooms),
      year_built: Number(data.year_built),
      lot_size: Number(data.lot_size),
      distance_to_city_center: Number(data.distance_to_city_center),
      school_rating: Number(data.school_rating),
    };

    try {
      // The ML API URL is used directly since it's a microservice in the same environment.
      const response = await fetchWithRetry(API_URL, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(payload),
      });

      const responseData = await response.json();

      if (response.ok && responseData.predicted_price !== undefined) {
        const predictedPrice = responseData.predicted_price;
        setResult(predictedPrice);

        const newEntry: PredictionHistory = {
            id: Date.now(),
            ...data, 
            predictedPrice: predictedPrice,
            timestamp: Date.now(),
        };
        setHistory(prev => [newEntry, ...prev.slice(0, 9)]); 
        // **********************************

      } else {
        setError(responseData.detail || "Prediction API failed to return a valid price.");
      }
    } catch (err) {
      console.error("API Error:", err);
      setError("Failed to connect to the prediction service. Please check the backend API.");
    } finally {
      setLoading(false);
    }
  };

  // 3. Helper component for input fields
  const InputField = ({ name, label, placeholder, type = "number" }: { name: keyof FormData, label: string, placeholder: string, type?: string }) => (
    <div>
      <label htmlFor={name} className="block text-gray-700 font-semibold mb-2">{label}</label>
      <input
        id={name}
        type={type}
        step={name === 'bathrooms' || name === 'school_rating' || name === 'distance_to_city_center' ? "any" : "1"}
        placeholder={placeholder}
        {...register(name)}
        className={`w-full px-6 py-4 border-2 rounded-xl text-lg transition duration-150 ease-in-out focus:ring-2 focus:ring-red-500 focus:border-red-500 ${errors[name] ? 'border-red-500 bg-red-50' : 'border-gray-300'}`}
      />
      {errors[name] && <p className="mt-2 text-sm text-red-500 font-medium">{errors[name]?.message}</p>}
    </div>
  );

  const chartData = result !== null ? [
      { name: 'Predicted Price', value: result, color: '#DC2626' },
      { name: 'Market Average', value: MARKET_TRAINING_MEAN_PRICE, color: '#FCD34D' },
  ] : [];

  return (
    <div className="max-w-4xl mx-auto p-8 bg-white rounded-3xl shadow-2xl">
      <h1 className="text-4xl font-extrabold text-center text-red-700 mb-10 border-b-4 border-red-100 pb-4">
        Property Value Estimator
      </h1>

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-8">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <InputField name="square_footage" label="Square Footage (sq ft)" placeholder="e.g., 1850" />
          <InputField name="bedrooms" label="Bedrooms (1-10)" placeholder="e.g., 3" />
          <InputField name="bathrooms" label="Bathrooms (1-10)" placeholder="e.g., 2.5" />
          <InputField name="year_built" label="Year Built" placeholder={`e.g., ${new Date().getFullYear() - 10}`} />
          <InputField name="lot_size" label="Lot Size (sq ft)" placeholder="e.g., 8200" />
          <InputField name="distance_to_city_center" label="City Center Rating (1-10)" placeholder="e.g., 4.8" />
          
          <div className="md:col-span-2">
            <InputField name="school_rating" label="School Rating (1-10)" placeholder="e.g., 8.7" />
          </div>
        </div>

        <button
          type="submit"
          disabled={loading}
          className="w-full bg-red-600 text-white py-6 rounded-2xl text-2xl font-bold hover:bg-red-700 disabled:opacity-50 transition flex items-center justify-center gap-4 shadow-lg"
        >
          {loading ? (
            <>
              <Loader2 className="animate-spin w-8 h-8" /> Predicting...
            </>
          ) : (
            "Estimate Property Value"
          )}
        </button>
      </form>

      {error && (
        <div className="mt-8 text-center bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-lg" role="alert">
          <p className="font-bold">Prediction Error</p>
          <p>{error}</p>
        </div>
      )}

      {result !== null && (
        <div className="mt-12 text-center bg-gradient-to-br from-red-50 to-red-100 rounded-3xl p-16 shadow-2xl">
          <p className="text-2xl text-red-500 font-semibold mb-4">The Estimated Value is:</p>
          <p className="text-6xl md:text-8xl font-black text-red-700">
            ${result.toLocaleString()}
          </p>
          <p className="text-4xl text-red-700 mt-6 font-bold">Estimated Property Value</p>
          
          <div className="mt-10 max-w-lg mx-auto p-6 bg-white rounded-2xl shadow-xl border border-red-200 text-left">
              <h3 className="text-2xl font-bold text-gray-800 mb-4 border-b pb-2">Input Details Summary</h3>
              <div className="grid grid-cols-2 gap-y-3 gap-x-4 text-sm">
                  {Object.entries(getValues()).map(([key, value]) => (
                      <div key={key} className="flex justify-between items-center border-b last:border-b-0 py-1">
                          <span className="font-semibold text-gray-600">{formatFeatureName(key)}:</span>
                          <span className="font-bold text-gray-900">
                              {value} {key === 'square_footage' || key === 'lot_size' ? 'sq ft' : ''}
                          </span>
                      </div>
                  ))}
              </div>
          </div>
          {/* ********************************* */}

          <div className="mt-10 w-full p-6 bg-white rounded-2xl shadow-xl border border-red-200">
            <h3 className="text-2xl font-bold text-gray-800 mb-4 border-b pb-2">Market Comparison</h3>
            <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                    <BarChart
                        data={chartData}
                        margin={{ top: 5, right: 30, left: 20, bottom: 5 }}
                    >
                        <CartesianGrid strokeDasharray="3 3" stroke="#e0e0e0"/>
                        <XAxis dataKey="name" stroke="#374151" />
                        <YAxis tickFormatter={(value) => `$${(value / 1000).toLocaleString()}k`} stroke="#374151" />
                        <Tooltip 
                            formatter={(value: number) => [`$${value.toLocaleString()}`, 'Price']} 
                            labelFormatter={(label) => label} 
                        />
                        <Legend />
                        <Bar dataKey="value" name="Price" fill="#DC2626" radius={[10, 10, 0, 0]} />
                    </BarChart>
                </ResponsiveContainer>
            </div>
            <p className="text-xs text-gray-400 mt-2">Comparison of predicted value vs. the training dataset's average price ($270,375).</p>
          </div>
          {/* ***************************** */}

          <p className="text-sm text-gray-500 mt-4">Prediction based on current ML model.</p>
        </div>
      )}
      
      {history.length > 0 && (
          <div className="mt-16 border-t pt-8">
              <h2 className="text-3xl font-extrabold text-gray-800 mb-8 text-center">Estimation History</h2>
              
              <div className="overflow-x-auto rounded-xl shadow-lg">
                  <table className="min-w-full divide-y divide-red-200">
                      <thead className="bg-red-50">
                          <tr>
                              <th className="px-6 py-4 text-left text-xs font-semibold text-red-600 uppercase tracking-wider">Time</th>
                              <th className="px-6 py-4 text-left text-xs font-semibold text-red-600 uppercase tracking-wider">Sq Footage</th>
                              <th className="px-6 py-4 text-left text-xs font-semibold text-red-600 uppercase tracking-wider">Beds/Baths</th>
                              <th className="px-6 py-4 text-left text-xs font-semibold text-red-600 uppercase tracking-wider">Year</th>
                              <th className="px-6 py-4 text-right text-xs font-semibold text-red-600 uppercase tracking-wider">Predicted Price</th>
                              <th className="px-6 py-4 text-center text-xs font-semibold text-red-600 uppercase tracking-wider">Action</th>
                          </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-100">
                          {history.map((item) => (
                              <tr key={item.id} className="hover:bg-red-50 transition duration-150">
                                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                      {new Date(item.timestamp).toLocaleTimeString()}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                                      {item.square_footage.toLocaleString()}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                                      {item.bedrooms} / {item.bathrooms}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                                      {item.year_built}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-right text-base font-bold text-red-700">
                                      ${item.predictedPrice.toLocaleString()}
                                  </td>
                                  <td className="px-6 py-4 whitespace-nowrap text-center text-sm">
                                      {/* 這裡是實現 a.v (比較視圖) 的起點 */}
                                      <button 
                                          className="text-red-600 hover:text-red-900 font-medium disabled:text-gray-400"
                                          // onClick={() => selectForComparison(item)} 
                                      >
                                          Compare
                                      </button>
                                  </td>
                              </tr>
                          ))}
                      </tbody>
                  </table>
              </div>
          </div>
      )}
      
    </div>
  );
}

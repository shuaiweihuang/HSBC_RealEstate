import { NextResponse } from 'next/server';

// Java API URL is read from the environment variables set in docker-compose.yml or Dockerfile
const JAVA_API_URL = process.env.NEXT_PUBLIC_JAVA_API_URL;

/**
 * Next.js Serverless API Route to proxy 'what-if' analysis requests to the Java Market API.
 */
export async function POST(request: Request) {
  if (!JAVA_API_URL) {
    return NextResponse.json({ error: "Java API URL is not configured" }, { status: 500 });
  }

  try {
    const features = await request.json();

    const res = await fetch(`${JAVA_API_URL}/api/properties/what-if`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(features),
    });

    if (!res.ok) {
      // Forward the error response status and body from the downstream service
      const errorBody = await res.json();
      return NextResponse.json(errorBody, { status: res.status });
    }

    const data = await res.json();
    return NextResponse.json(data);

  } catch (error) {
    console.error('Proxy Error:', error);
    return NextResponse.json({ error: 'Failed to connect to the backend API service.' }, { status: 500 });
  }
}

import psycopg2

url = "postgresql://carbon_casecampby:29cd109da69de247ab305bb7e1f73bcd4904d405@8s1p9p.h.filess.io:5434/carbon_casecampby"

try:
    conn = psycopg2.connect(url)
    print("✅ Connected successfully!")
    conn.close()
except Exception as e:
    print("❌ Connection failed:", e)

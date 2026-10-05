#!/usr/bin/env python3
"""Run deterministic HTTP acceptance checks. Start the backend first. No Ollama required."""
import json
import sys
from urllib.request import Request, urlopen
from urllib.error import HTTPError
BASE = sys.argv[1].rstrip('/') if len(sys.argv) > 1 else 'http://127.0.0.1:8080'
def request(path, payload=None):
    body = json.dumps(payload).encode() if payload is not None else None
    req = Request(BASE + path, data=body, headers={'Content-Type': 'application/json'})
    try:
        with urlopen(req, timeout=10) as response:
            return response.status, json.load(response)
    except HTTPError as ex:
        return ex.code, json.load(ex)
checks = 0
for service, health in [('payment-service','UNHEALTHY'),('order-service','HEALTHY'),('inventory-service','UNHEALTHY'),('unknown-service','UNKNOWN')]:
    status, result = request('/api/ai/investigate', {'serviceName':service,'mode':'demo'})
    assert status == 200, result
    assert result['service'] == service and result['health'] == health, result
    assert result['demoData'] is True and result['analysisSource'] != 'OLLAMA', result
    if health == 'HEALTHY': assert result['recommendedChecks'] == [], result
    if health == 'UNKNOWN': assert result['evidence'] == ['DEMO health: UNKNOWN'], result
    checks += 1
    print('PASS', service, health)
for path in ['/api/ai/investigate', '/api/ai/investigate?serviceName=', '/api/ai/investigate?serviceName=order-service&mode=invalid']:
    status, result = request(path)
    assert status == 400, result
    checks += 1
    print('PASS invalid request -> 400')
print(f'{checks} acceptance checks passed. This does not evaluate live LLM quality.')

import urllib.request
import json

# 获取最新的运行
url = "https://api.github.com/repos/critallu/SmartNotepad/actions/runs?per_page=1"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as resp:
    runs = json.load(resp)["workflow_runs"]
    run = runs[0]
    print(f"Run ID: {run['id']}")

# 获取 jobs
url = f"https://api.github.com/repos/critallu/SmartNotepad/actions/runs/{run['id']}/jobs"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as resp:
    data = json.load(resp)
    job_id = data["jobs"][0]["id"]
    print(f"Job ID: {job_id}")

# 获取步骤日志 - 使用 logs 接口
url = f"https://api.github.com/repos/critallu/SmartNotepad/actions/jobs/{job_id}/logs"
req = urllib.request.Request(url)
# 需要设置 Accept header 来获取文本日志
req.add_header("Accept", "application/vnd.github+json")
try:
    with urllib.request.urlopen(req) as resp:
        content = resp.read().decode("utf-8")
        # 只打印包含 error 或 FAILED 或 error 的部分
        lines = content.split("\n")
        for i, line in enumerate(lines):
            lower = line.lower()
            if "error" in lower or "fail" in lower or "exception" in lower or "could not" in lower or "not found" in lower or "unresolved" in lower:
                # 打印前后5行
                start = max(0, i-3)
                end = min(len(lines), i+5)
                print(f"--- Line {start+1}-{end} ---")
                for j in range(start, end):
                    print(f"  {lines[j]}")
                print()
except Exception as e:
    print(f"Error: {e}")
    # 尝试另一种方式获取日志
    url2 = f"https://github.com/critallu/SmartNotepad/actions/runs/{run['id']}/job/{job_id}"
    print(f"Open in browser: {url2}")

import urllib.request
import json
import gzip
import io

# 获取最新的运行
url = "https://api.github.com/repos/critallu/SmartNotepad/actions/runs?per_page=1"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as resp:
    runs = json.load(resp)["workflow_runs"]
    run = runs[0]
    print(f"Run ID: {run['id']}")

# 获取日志下载链接
url = f"https://api.github.com/repos/critallu/SmartNotepad/actions/runs/{run['id']}/logs"
req = urllib.request.Request(url)
with urllib.request.urlopen(req) as resp:
    # 下载日志zip
    data = resp.read()
    with gzip.GzipFile(fileobj=io.BytesIO(data)) as f:
        content = f.read().decode("utf-8")
        # 只打印前5000个字符
        print(content[:5000])

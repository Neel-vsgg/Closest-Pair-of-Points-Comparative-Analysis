#!/usr/bin/env python3
"""
plot.py
Usage:
  python plot.py java_output.txt
Produces:
  - points_plot.png
  - runtime_plot.png
"""
import sys
import matplotlib.pyplot as plt

def parse_java_output(path):
    bf_line = None
    dc_line = None
    points = []
    midX = None
    d = None
    perf_samples = []
    reading_points = False
    with open(path, 'r') as f:
        for raw in f:
            ln = raw.strip()
            if not ln:
                continue
            if ln.startswith("Brute-Force:"):
                bf_line = ln
            elif ln.startswith("Divide-and-Conquer:"):
                dc_line = ln
            elif ln == "POINTS_CSV":
                reading_points = True
            elif ln.startswith("DIVIDE_INFO"):
                reading_points = False
                parts = ln.split(",")
                if len(parts) >= 3:
                    try:
                        midX = float(parts[1])
                        d = float(parts[2])
                    except:
                        pass
            elif reading_points:
                try:
                    x,y = ln.split(",")
                    points.append((float(x), float(y)))
                except:
                    pass
            else:
                parts = ln.split(",")
                if len(parts) == 3:
                    try:
                        N = int(parts[0])
                        bf_ms = float(parts[1])
                        dc_ms = float(parts[2])
                        perf_samples.append((N, bf_ms, dc_ms))
                    except:
                        pass
    return bf_line, dc_line, points, midX, d, perf_samples

def extract_pair(line):
    if not line:
        return None, None, None
    try:
        rest = line.split(":",1)[1].strip()
        dist_str, coords = rest.split(" ",1)
        coords = coords.strip()
        parts = coords.split()
        a = parts[0].strip().strip("()")
        b = parts[1].strip().strip("()")
        ax, ay = map(float, a.split(","))
        bx, by = map(float, b.split(","))
        return float(dist_str), (ax,ay), (bx,by)
    except Exception:
        return None, None, None

def plot_points(points, bf_pair, dc_pair, midX, d, out_prefix="points_plot"):
    if not points:
        print("No points found in input.")
        return
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    plt.figure(figsize=(8,8))
    plt.scatter(xs, ys, s=12, color='gray', alpha=0.6, label='points')
    if bf_pair[1] and bf_pair[2]:
        plt.plot([bf_pair[1][0], bf_pair[2][0]], [bf_pair[1][1], bf_pair[2][1]],
                 color='red', linewidth=2, label='Brute-Force pair')
        plt.scatter([bf_pair[1][0], bf_pair[2][0]], [bf_pair[1][1], bf_pair[2][1]], color='red')
    if dc_pair[1] and dc_pair[2]:
        plt.plot([dc_pair[1][0], dc_pair[2][0]], [dc_pair[1][1], dc_pair[2][1]],
                 color='blue', linewidth=2, label='Divide-Conquer pair')
        plt.scatter([dc_pair[1][0], dc_pair[2][0]], [dc_pair[1][1], dc_pair[2][1]], color='blue')
    if midX is not None and d is not None:
        plt.axvline(midX, color='green', linestyle='--', label='midX')
        plt.axvspan(midX - d, midX + d, color='green', alpha=0.08, label='strip (2d)')
    plt.legend()
    plt.xlabel('x')
    plt.ylabel('y')
    plt.title('Closest pairs visualization')
    plt.tight_layout()
    out = f"{out_prefix}.png"
    plt.savefig(out, dpi=150)
    plt.close()
    print("Saved:", out)

def plot_runtime(perf_samples, out_prefix="runtime_plot"):
    if not perf_samples:
        print("No performance samples found in Java output.")
        return
    perf_samples.sort()
    Ns = [p[0] for p in perf_samples]
    bf = [p[1] for p in perf_samples]
    dc = [p[2] for p in perf_samples]
    plt.figure(figsize=(8,5))
    plt.plot(Ns, bf, marker='o', label='Brute-Force')
    plt.plot(Ns, dc, marker='o', label='Divide-and-Conquer')
    plt.xlabel('N')
    plt.ylabel('Time (ms)')
    plt.title('Runtime vs N')
    plt.legend()
    plt.grid(True, linestyle='--', alpha=0.4)
    plt.tight_layout()
    out = f"{out_prefix}.png"
    plt.savefig(out, dpi=150)
    plt.close()
    print("Saved:", out)

def main():
    if len(sys.argv) < 2:
        print("Usage: python plot.py java_output.txt")
        return
    path = sys.argv[1]
    bf_line, dc_line, points, midX, d, perf_samples = parse_java_output(path)
    bf = extract_pair(bf_line)
    dc = extract_pair(dc_line)
    print("Parsed points:", len(points))
    print("Brute-Force:", bf_line)
    print("Divide-and-Conquer:", dc_line)
    plot_points(points, bf, dc, midX, d)
    plot_runtime(perf_samples)

if __name__ == "__main__":
    main()

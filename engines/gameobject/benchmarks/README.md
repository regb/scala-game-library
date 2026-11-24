# GameObject engine benchmarks

Frames per second and resource usage of the engine, on the headless OpenGL
backend (Linux, no window and no vsync).

```bash
bazel run //engines/gameobject/benchmarks                                     # every scenario, 10000 things each
bazel run //engines/gameobject/benchmarks -- --list                           # what there is
bazel run //engines/gameobject/benchmarks -- --scenarios sprites,hierarchy --count 2000
```

To measure a change, save a run before it and compare after it:

```bash
bazel run //engines/gameobject/benchmarks -- --save /tmp/before.tsv
bazel run //engines/gameobject/benchmarks -- --compare /tmp/before.tsv
```

| Option | Default | |
| --- | --- | --- |
| `--scenarios a,b` | all | Which scenarios to run |
| `--count n` | 10000 | Sprites, meshes or tiles in each scenario |
| `--seconds s` | 5 | How long each scenario is measured |
| `--warmup s` | 4 | How long it runs before that, for the JIT |
| `--runs n` | 1 | Run each scenario this many times and keep the fastest, on a machine that is not quiet |
| `--software` | off | Render on the CPU with Mesa, to compare without a GPU |
| `--save file`, `--compare file` | | Write the results, or show the change from saved ones |
| `--screenshots dir` | | Save what each scenario draws, to check that it is what it says |

Each scenario runs in a JVM of its own with a fixed 1 GB heap. Every frame ends
with `glFinish`, so the times include the GPU's work. That keeps the CPU and the
GPU from overlapping: a game with a window can be somewhat faster than this.

| Column | |
| --- | --- |
| `fps`, `p50`, `p95`, `p99` | Frames per second, and percentiles of the frame time in ms |
| `update` | CPU ms from `PreUpdate` up to the `Render` stage |
| `submit` | CPU ms in the systems of the `Render` stage |
| `draw` | CPU ms composing the frame and calling OpenGL, after that stage |
| `gpu wait` | ms until the GPU has finished the frame |
| `KB/frame` | Allocated by the game thread |
| `GCs/s`, `GC %` | Collections, and the share of the time they take |
| `heap MB`, `RSS MB` | Live heap after a full collection, and memory of the process |
| `CPU %` | Of one core, all threads of the process included |

Close other programs, keep a laptop plugged in, and run twice before believing a
small difference. Results from different machines cannot be compared.

A scenario is a block that populates the scene: add one to `Scenarios.all` in
`BenchmarkApp.scala`. It is not part of `bazel test`, which would only measure the CI machine.

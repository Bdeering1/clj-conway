# clj-conway

[Conway's Game of Life](https://conwaylife.com/wiki/Conway%27s_Game_of_Life) (CGL) implemented in Clojure.

CGL is a discrete event simulation played out on a 2D grid of points (or "cells"), where at any slice of time each cell is either alive or dead. Each iteration, the next state of each cell is determined by a simple set of rules:
1. Any living cell with less than two neighbours dies due to underpopulation.
2. Any living cell with more than three neighbours dies due to overpopulation.
3. Any living cell with two or three neighbours lives on to the next generation.
4. Any dead cell with exactly three living neighbours will come to life.

## Usage

Run using the provided jar file (in the "releases" tab), or build from source.

To build from source, you'll need [leiningen](https://leiningen.org/).

Once installed, cd into the project directory and run using:
```
lein run -m clj-conway.core
```

Once running, toggle points by clicking on the grid, and play/pause the simulation by pressing space.

Some other controls:

| Keybind    | Action             |
|------------|--------------------|
| ↓, ↑, ←, → | pan                |
| -, +       | zoom in/out        |
| 0          | clear grid         |
| 1-5        | load template      |
| c          | reset pan (center) |
| r          | reset pan + zoom   |
| x          | toggle 2x speed    |


*this project was developed using Java Temurin 17, but should work with other versions of Java as well

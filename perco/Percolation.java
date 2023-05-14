import edu.princeton.cs.algs4.UF;


/**
 * Performance requirements.  The constructor should take time proportional to N2;
 * all methods should take constant time plus a constant number of calls
 * to the union-find methods union(), find(), connected(), and count().
 */
public class Percolation {
    private int cntOpen; // cnt open sites
    private int size; // grid size
    private int[][] sites; // imitate the grid
    private boolean percolate; // if grid percolates
    /**
     * leader of topUf is N * N
     * leader of bottomUf is N * N + 1
     */
    private UF bottomUf, topUf;

    //
    public Percolation(int N) {
        cntOpen = 0;
        size = N;
        sites = new int[size][size];
        percolate = false;
        topUf = new UF(N * N + 2);
        bottomUf = new UF(N * N + 2);
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                sites[i][j] = 0;
            }
        }
    }

    private int twoTOoneDimen(int i, int j) {
        return j + i * size;
    }

    private boolean connectTop(int i, int j) {
        return topUf.connected(twoTOoneDimen(i, j), size * size);
    }

    private boolean connectBottom(int i, int j) {
        return bottomUf.connected(twoTOoneDimen(i, j), size * size + 1);
    }

    private boolean validatePos(int i, int j) {
        return i < size && j < size && i > -1 && j > -1;
    }

    private int abs(int x) {
        return x < 0 ? -x : x;
    }

    private boolean validateDir(int x, int y) {
        return abs(x) + abs(y) == 1;
    }

    public void open(int i, int j) {
        sites[i][j] = 1;
        cntOpen++;
        if (i == 0) {
            topUf.union(twoTOoneDimen(i, j), size * size);
        }
        else if (i == size - 1) {
            bottomUf.union(twoTOoneDimen(i, j), size * size + 1);
        }
        for (int x = -1; x < 2; x++) {
            for (int y = -1; y < 2; y++) {
                if (validateDir(x, y) && validatePos(i + x, j + y) && isOpen(i + x, j + y)) {
                    topUf.union(twoTOoneDimen(i, j), twoTOoneDimen(i + x, j + y));
                    bottomUf.union(twoTOoneDimen(i, j), twoTOoneDimen(i + x, j + y));
                }
            }
        }
        if (connectBottom(i, j) && connectTop(i, j)) {
            percolate = true;
        }
    }

    //
    public boolean isOpen(int i, int j) {
        return sites[i][j] == 1;
    }

    public boolean isFull(int i, int j) {
        return isOpen(i, j) && connectTop(i, j);
    }

    public int numberOfOpenSites() {
        return cntOpen;
    }

    public boolean percolates() {
        return percolate;
    }

    public static void main(String[] args) {
    }
}




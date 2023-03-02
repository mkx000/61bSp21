package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author TODO
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                if (!validateArgs(args, 1)) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.initRepo();
                break;
            case "add":
                if (!validateArgs(args, 2)) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            case "checkout":
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    private static boolean validateArgs(String[] args, int n) {
        if (args.length == n) {
            return true;
        } else {
            return false;
        }
    }
}

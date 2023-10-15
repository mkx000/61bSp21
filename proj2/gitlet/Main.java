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
        if (args == null || args.length == 0) {
            Utils.exitWithMessage("Please enter a command.");
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init":
                validateNumArgs(args, 1);
                Repository.init();
                break;
            case "add":
                validateRepoExists();
                validateNumArgs(args, 2);
                Repository.add(args[1]);
                break;
            case "commit":
                validateRepoExists();
                if (args.length == 1 || args[1].equals("")) {
                    Utils.exitWithMessage("Please enter a commit message.");
                }
                validateNumArgs(args, 2);
                Repository.commit(args[1]);
                break;
            case "rm":
                validateRepoExists();
                validateNumArgs(args, 2);
                Repository.rm(args[1]);
                break;
            case "log":
                validateRepoExists();
                validateNumArgs(args, 1);
                Repository.log();
                break;
            case "global-log":
                validateRepoExists();
                validateNumArgs(args, 1);
                Repository.globalLog();
                break;
            case "find":
                validateRepoExists();
                validateNumArgs(args, 2);
                Repository.find(args[1]);
                break;
            case "status":
                validateRepoExists();
                validateNumArgs(args, 1);
                Repository.status();
                break;
            case "checkout":
                validateRepoExists();
                if (args.length < 2 || args.length > 4) {
                    Utils.exitWithMessage("Incorrect operands.");
                }
                Repository.checkout(args);
                break;
            default:
                System.out.println("No command with that name exists.");
                System.exit(0);
        }
    }

    public static void validateNumArgs(String[] args, int num) {
        if (args.length != num) {
            Utils.exitWithMessage("Incorrect operands.");
        }
    }

    public static void validateRepoExists() {
        if (!Repository.GITLET_DIR.exists()) {
            Utils.exitWithMessage("Not in an initialized Gitlet directory.");
        }
    }
}

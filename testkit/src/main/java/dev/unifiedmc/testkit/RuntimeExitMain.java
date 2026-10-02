package dev.unifiedmc.testkit;

/** Child process used only by runtime integration tests. */
public final class RuntimeExitMain {
    private RuntimeExitMain() {}

    public static void main(String[] args) throws Exception {
        int exit = Integer.parseInt(args.length == 0 ? "0" : args[0]);
        System.out.println("runtime-fixture-stdout");
        System.err.println("runtime-fixture-stderr");
        if (exit < 0) {
            Thread.sleep(Math.abs(exit));
            return;
        }
        System.exit(exit);
    }
}

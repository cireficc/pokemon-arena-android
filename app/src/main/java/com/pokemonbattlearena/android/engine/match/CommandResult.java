package com.pokemonbattlearena.android.engine.match;

public class CommandResult {

    TargetInfo targetInfo;
    StringBuilder logBuilder;

    protected CommandResult() {
        this.logBuilder = new StringBuilder();
    }

    public TargetInfo getTargetInfo() {
        return targetInfo;
    }

    protected void appendToLog(String s) {
        this.logBuilder.append(s).append("\n");
    }

    public String getLog() {
        return logBuilder.toString();
    }
}

package ca.zhoozhoo.loaddev.mcp.config;

import reactor.util.context.ContextView;

public class ReactiveContextHolder {

    public static final ThreadLocal<ContextView> reactiveContext = new ThreadLocal<>();
}

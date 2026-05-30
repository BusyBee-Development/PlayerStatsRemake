package com.fernsehheft.playerstatsremake.core.statistic;

import com.fernsehheft.playerstatsremake.api.StatRequest;
import com.fernsehheft.playerstatsremake.api.StatResult;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;

public abstract class RequestProcessor {

    abstract @NotNull StatResult<Integer> processPlayerRequest(StatRequest<?> playerStatRequest);

    abstract @NotNull StatResult<Long> processServerRequest(StatRequest<?> serverStatRequest);

    abstract @NotNull StatResult<LinkedHashMap<String, Integer>> processTopRequest(StatRequest<?> topStatRequest);
}

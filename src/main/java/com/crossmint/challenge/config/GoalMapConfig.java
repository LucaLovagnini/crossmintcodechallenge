package com.crossmint.challenge.config;

import com.crossmint.challenge.service.AstralObjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoalMapConfig {
    private static final Logger logger = LoggerFactory.getLogger(GoalMapConfig.class);

    @Bean
    public GoalMap gridConfig(AstralObjectService astralObjectService) {
        return astralObjectService.getGoalMap();
    }

}

package com.xbx.study.ai.entity.vo;

import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AgentModeInfoVo {


    private Map<String,Model> agents;

    private final Boolean a2uiEnabled;

    private final String version;

    private final String licenseStatus ;


    public AgentModeInfoVo(AgentModelBuilder builder){
        if (CollectionUtils.isEmpty(builder.agents)){
            throw new IllegalArgumentException("至少设置一个模型信息");
        }
        this.a2uiEnabled = builder.a2uiEnabled;
        this.version = builder.version;
        this.licenseStatus = builder.licenseStatus;

        this.agents = new HashMap<>();
        for (Model agent : builder.agents) {
            this.agents.put(agent.id, agent);
        }


    }


    public static AgentModelBuilder builder() {
        return new AgentModelBuilder();
    }

    public Map<String, Model> getAgents() {
        return agents;
    }


    public Boolean getA2uiEnabled() {
        return a2uiEnabled;
    }


    public String getVersion() {
        return version;
    }


    public String getLicenseStatus() {
        return licenseStatus;
    }


    public record Model(String id, String description, String name) {
    }


    public static class AgentModelBuilder {
        private Boolean a2uiEnabled  = false;
        private String version;
        private String licenseStatus = "valid";
        private List<Model> agents;



        public AgentModelBuilder A2uiEnabled(Boolean a2uiEnabled) {
            this.a2uiEnabled = a2uiEnabled;
            return this;
        }

        public AgentModelBuilder version(String version) {
            this.version = version;
            return this;
        }
        public AgentModelBuilder licenseStatus(String licenseStatus) {
            this.licenseStatus = licenseStatus;
            return this;
        }

        public AgentModelBuilder agents(Model agents) {

            if (this.agents == null) {
                this.agents = new ArrayList<>();
            }

            this.agents.add(agents);
            return this;
        }

        public AgentModeInfoVo build() {
            return new AgentModeInfoVo(this);
        }



    }




}

package com.ai.langgraph.service;


import com.ai.langgraph.mapper.DeployStateMapper;
import com.ai.langgraph.service.langgraph.deploy.DeployState;
import com.baomidou.mybatisplus.spring.service.IService;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AgentStateService extends ServiceImpl<DeployStateMapper, DeployState> implements IService<DeployState> {
}

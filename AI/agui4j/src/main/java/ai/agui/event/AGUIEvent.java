package ai.agui.event;


import ai.agui.enums.AgUiEventType;

public abstract class AGUIEvent {

    private final AgUiEventType type;

    protected AGUIEvent(AgUiEventType type){
        this.type = type;
    }

    public AgUiEventType getType(){
        return type;
    }

}



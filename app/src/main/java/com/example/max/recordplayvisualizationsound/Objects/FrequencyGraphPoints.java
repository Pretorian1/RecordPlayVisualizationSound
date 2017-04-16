package com.example.max.recordplayvisualizationsound.Objects;

import org.simpleframework.xml.Default;

import java.util.List;

/**
 * Created by Max on 16.04.2017.
 */
@Default
public class FrequencyGraphPoints {
    private List<FrequencyGraphPoint> frequencyGraphPointList;

    public List<FrequencyGraphPoint> getFrequencyGraphPointList() {
        return frequencyGraphPointList;
    }

    public void setFrequencyGraphPointList(List<FrequencyGraphPoint> frequencyGraphPointList) {
        this.frequencyGraphPointList = frequencyGraphPointList;
    }
}

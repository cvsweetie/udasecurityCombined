package com.udacity.catpoint.securityservice.data;

import java.util.HashSet;
import java.util.Set;

public class SecurityRepositoryImpl implements SecurityRepository {
    private Set<Sensor> sensors = new HashSet<>();
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;

    @Override
    public void addSensor(Sensor sensor) {
        sensors.add(sensor);
    }

    @Override
    public void removeSensor(Sensor sensor) {
        sensors.remove(sensor);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        for (Sensor a : sensors) {
            if (a.getSensorId()== sensor.getSensorId() ) {
                a.setActive(sensor.getActive());
                a.setName(sensor.getName());
                a.setSensorType(sensor.getSensorType());
            }
        }
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = armingStatus;
    }

    @Override
    public Set<Sensor> getSensors() {
        return new HashSet<>(sensors);
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }
}

package com.udacity.catpoint.securityservice;



import com.udacity.catpoint.imageservice.ImageService;
import com.udacity.catpoint.securityservice.application.StatusListener;
import com.udacity.catpoint.securityservice.data.AlarmStatus;
import com.udacity.catpoint.securityservice.data.ArmingStatus;
import com.udacity.catpoint.securityservice.data.SecurityRepository;
import com.udacity.catpoint.securityservice.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {
    private ImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();
    private boolean catDetected = false;

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

//  Commented out as the method is unused and not correct in returning a parameter for this service
//    public ImageService getImageService() {
//        return imageService;
//    }

//  Commented out as the method is unused and not correct in setting parameter used to create this service instance
//    public void setImageService(ImageService imageService) {
//        this.imageService = imageService;
//    }

//   Service should safely handle changes. Repo should not be exposed to external consumer. Method not used anyway.
//    public SecurityRepository getSecurityRepository() {
//        return securityRepository;
//    }
//   Service should safely handle changes. Repo should not be exposed to external consumer. Method not used anyway.
//    public void setSecurityRepository(SecurityRepository securityRepository) {
//        this.securityRepository = securityRepository;
//    }


//    Status changes should be handled safely by this service
//    public Set<StatusListener> getStatusListeners() {
//        return statusListeners;
//    }
//
//    public void setStatusListeners(Set<StatusListener> statusListeners) {
//        this.statusListeners = statusListeners;
//    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        // If armingStatus is ARMED, set all sensors inactive
        if(armingStatus == ArmingStatus.ARMED_HOME || armingStatus == ArmingStatus.ARMED_AWAY) {
            ConcurrentSkipListSet<Sensor> safeSensors =  new ConcurrentSkipListSet<>(this.getSensors());
            safeSensors.stream().forEach(s -> {
                this.changeSensorActivationStatus(s, false);
            });
            if(catDetected) {
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }
        securityRepository.setArmingStatus(armingStatus);
    }

    private boolean areAllSensorsInactive() {
        for (Sensor sensor : this.getSensors()) {
            if (sensor.getActive()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        } else {
            if (areAllSensorsInactive()) {
                setAlarmStatus(AlarmStatus.NO_ALARM);
            }
        }
        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
            default -> {  }
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
        switch(securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
            case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            default -> {

            }
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        if(!sensor.getActive() && active) {
            handleSensorActivated();
        } else if (sensor.getActive() && !active) {
            if (getAlarmStatus() != AlarmStatus.ALARM){
                handleSensorDeactivated();
            }
        } else if (sensor.getActive() && active) {
            if (getAlarmStatus() == AlarmStatus.PENDING_ALARM){
                setAlarmStatus(AlarmStatus.ALARM);
            }
        }

        sensor.setActive(active);
        securityRepository.updateSensor(sensor);
        statusListeners.forEach(sl -> sl.sensorStatusChanged());
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected = imageService.imageContainsCat(currentCameraImage, 50.0f);
        catDetected(catDetected);
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }
}

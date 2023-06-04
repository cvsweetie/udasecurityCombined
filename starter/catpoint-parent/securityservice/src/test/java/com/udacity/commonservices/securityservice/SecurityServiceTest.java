package com.udacity.commonservices.securityservice;

import com.udacity.catpoint.imageservice.ImageService;
import com.udacity.catpoint.securityservice.SecurityService;
import com.udacity.catpoint.securityservice.data.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class SecurityServiceTest {
    @Mock
    private ImageService imageService;
    @Mock
    private SecurityRepository securityRepository;
    private SecurityService securityService;
    Set<Sensor> sensors;

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
        Sensor sensorA = new Sensor("sensorA", SensorType.DOOR);
        Sensor sensorB = new Sensor("sensorB", SensorType.WINDOW);
        sensors = new HashSet<>();
        sensors.add(sensorA);
        sensors.add(sensorB);
    }

    @AfterEach
    void destroy() {
        sensors = null;
    }

    /**
     * 1.If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
     */

    @Test
    void whenAlarmOn_And_SensorActive_Then_SystemPending(){
      //  securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
     //   securityService.setAlarmStatus(AlarmStatus.NO_ALARM);

        //when( securityRepository.getSensors()).thenReturn(sensors);
        when( securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when (securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];
        securityService.changeSensorActivationStatus(sensorToActivate, true);
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());

        assertEquals(captor.getValue(), AlarmStatus.PENDING_ALARM);

    }

    /**
     *2. If alarm is armed and a sensor becomes activated and the system is already pending alarm,
     * set the alarm status to alarm.
     */
    @Test
    void whenAlarmOn_SensorActive_And_SystemPending_Then_AlarmStatus_Alarm(){
        when( securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when (securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];
        securityService.changeSensorActivationStatus(sensorToActivate, true);
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);

        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);

    }

    /**
     * 3. If pending alarm and all sensors are inactive, return to no alarm state.
     */
   @Test
    void whenPendingAlarm_And_AllSensorsInactive_Then_NoAlarmState(){
       when( securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
       // when (securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
       Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];
       sensorToActivate.setActive(true);
       securityService.changeSensorActivationStatus(sensorToActivate, false);
       ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
       verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
       assertEquals(captor.getValue(), AlarmStatus.NO_ALARM);
    }

    /**
     * 4. If alarm is active, change in sensor state should not affect the alarm state.
     */
    @Test
    void whenAlarmActive_Then_ChangeInSensorState_NotAffectAlarm(){
        Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];
        securityService.changeSensorActivationStatus(sensorToActivate, false);
        verify(securityRepository, never()).setAlarmStatus(any());

        when( securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensorToActivate, true);
        verify(securityRepository, never()).setAlarmStatus(any());

        sensorToActivate.setActive(true);
        securityService.changeSensorActivationStatus(sensorToActivate, false);
        verify(securityRepository, never()).setAlarmStatus(any());

        securityService.changeSensorActivationStatus(sensorToActivate, true);
        verify(securityRepository, never()).setAlarmStatus(any());



//        assertEquals(captor.getValue(),AlarmStatus.ALARM);

    }
    /**
     * 5. If a sensor is activated while already active and the system is in pending state, change it to alarm state.
     */
    @Test
    void whenSensorActivated_whileAlreadyActive_And_SystemPending_Then_SetAlarm(){
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];
        sensorToActivate.setActive(true);
        securityService.changeSensorActivationStatus(sensorToActivate, true);
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);
    }
    /**
     * 6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
     */
    @Test
    void whenSensorDeactivated_whileAlreadyInactive_Then_NoChange(){
        Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];
        sensorToActivate.setActive(false);
        securityService.changeSensorActivationStatus(sensorToActivate, false);
        verify(securityRepository,times(0)).setAlarmStatus(any());
    }
    /**
     * 7. If the image service identifies an image containing a cat while the system is armed-home,
     * put the system into alarm status.
     */
    @Test
    void whenImage_IdentifiedAs_Cat_Then_AlarmStatus() {
        when (securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        BufferedImage catImage = new BufferedImage(1,1,1);
        securityService.processImage(catImage);
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);
    }
    /**
     * 8. If the image service identifies an image that does not contain a cat,
     * change the status to no alarm as long as the sensors are not active.
     */
    @Test
    void whenImageNotCat_And_SensorsNotActive_Then_ChangeToNoAlarm(){
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        Sensor sensorToActivate = (Sensor) (sensors.stream().toArray())[0];

        sensorToActivate.setActive(false);
        BufferedImage catImage = new BufferedImage(1,1,1);

        securityService.processImage(catImage);

        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.NO_ALARM);
    }
    /**
     * 9. If the system is disarmed, set the status to no alarm.
     */
    @Test
    void whenSystemDisArmed_Then_SetToNoAlarm(){
      //  when (securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);

        securityService.setArmingStatus(ArmingStatus.DISARMED);
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.NO_ALARM);
    }
    /**
     * 10. If the system is armed, reset all sensors to inactive.
     */
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    void whenSystemArmed_Then_ResetSensorsToInactive(ArmingStatus armingStatus){
        when (securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(armingStatus);
        for (Sensor s: securityService.getSensors()) {
            assertEquals(s.getActive(), false);
        }
    }

    /**
     * 11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
     */
    @Test
    void whenArmedHome_And_CameraShowsCat_Then_SetToAlarm(){
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        BufferedImage catImage = new BufferedImage(1,1,1);
        securityService.processImage(catImage);
        ArgumentCaptor<AlarmStatus> captor = ArgumentCaptor.forClass(AlarmStatus.class);
        verify(securityRepository, atMost(1)).setAlarmStatus(captor.capture());
        assertEquals(captor.getValue(), AlarmStatus.ALARM);
    }
}

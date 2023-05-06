package in.fivedegree.securityapp;

public class ReadWriteUserDetails {

    public String email, registerDate, registerTime, loginDate, loginTime, deviceName, latitude, longitude, address, frontPhoto, rearPhoto, monitorFrequency, isMonitoring, ringMode, flash, playSound;
    public Integer batteryLevel;

    public ReadWriteUserDetails(String email, String registerDate, String registerTime, String loginDate, String loginTime, String deviceName, String latitude, String longitude, String address, String frontPhoto, String rearPhoto, String monitorFrequency, String isMonitoring, String ringMode, String flash, String playSound, Integer batteryLevel) {
        this.email = email;
        this.registerDate = registerDate;
        this.registerTime = registerTime;
        this.loginDate = loginDate;
        this.loginTime = loginTime;
        this.deviceName = deviceName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.frontPhoto = frontPhoto;
        this.rearPhoto = rearPhoto;
        this.monitorFrequency = monitorFrequency;
        this.isMonitoring = isMonitoring;
        this.ringMode = ringMode;
        this.flash = flash;
        this.playSound = playSound;
        this.batteryLevel = batteryLevel;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getRegisterDate() {
        return registerDate;
    }
    public void setRegisterDate(String registerDate) {
        this.registerDate = registerDate;
    }

    public String getRegisterTime() {
        return registerTime;
    }
    public void setRegisterTime(String registerTime) {
        this.registerTime = registerTime;
    }

    public String getDeviceName() {
        return deviceName;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getLoginDate() {
        return loginDate;
    }
    public void setLoginDate(String loginDate) {
        this.loginDate = loginDate;
    }

    public String getLoginTime() {
        return loginTime;
    }
    public void setLoginTime(String loginTime) {
        this.loginTime = loginTime;
    }

    public String getLatitude() {
        return latitude;
    }
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

    public String getFrontPhoto() {
        return frontPhoto;
    }
    public void setFrontPhoto(String frontPhoto) {
        this.frontPhoto = frontPhoto;
    }

    public String getRearPhoto() {
        return rearPhoto;
    }
    public void setRearPhoto(String rearPhoto) {
        this.rearPhoto = rearPhoto;
    }

    public String getMonitorFrequency() {
        return monitorFrequency;
    }
    public void setMonitorFrequency(String monitorFrequency){this.monitorFrequency = monitorFrequency;}

    public String getRingMode() {
        return ringMode;
    }
    public void setRingMode(String ringMode) {
        this.ringMode = ringMode;
    }

    public String getFlash() {
        return flash;
    }
    public void setFlash(String flash) {
        this.flash = flash;
    }

    public String getPlaySound() {
        return playSound;
    }
    public void setPlaySound(String playSound) {
        this.playSound = playSound;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }
    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }
}

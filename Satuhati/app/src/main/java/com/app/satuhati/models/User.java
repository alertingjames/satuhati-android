package com.app.satuhati.models;

import com.google.android.gms.maps.model.LatLng;

public class User {
    int _idx = 0;
    String _name = "";
    String _email = "";
    String _password = "";
    String _photoUrl = "";
    LatLng latLng = null;
    String _status = "";
    String _registered_time = "";

    public User(){}

    public int get_idx() {
        return _idx;
    }

    public void set_idx(int _idx) {
        this._idx = _idx;
    }

    public String get_name() {
        return _name;
    }

    public void set_name(String _name) {
        this._name = _name;
    }

    public String get_email() {
        return _email;
    }

    public void set_email(String _email) {
        this._email = _email;
    }

    public String get_password() {
        return _password;
    }

    public void set_password(String _password) {
        this._password = _password;
    }

    public String get_photoUrl() {
        return _photoUrl;
    }

    public void set_photoUrl(String _photoUrl) {
        this._photoUrl = _photoUrl;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public String get_status() {
        return _status;
    }

    public void set_status(String _status) {
        this._status = _status;
    }

    public String get_registered_time() {
        return _registered_time;
    }

    public void set_registered_time(String _registered_time) {
        this._registered_time = _registered_time;
    }
}

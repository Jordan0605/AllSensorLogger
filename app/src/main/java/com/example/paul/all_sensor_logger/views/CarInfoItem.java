package com.example.paul.all_sensor_logger.views;

/**
 * Created by User on 2017/4/17.
 */

public class CarInfoItem {
    private String name;
    private String carType;
    private String carAge;
    private Boolean isSelected = false;

    public CarInfoItem(String Name, String CarType, String CarAge)
    {
        name = Name;
        carType = CarType;
        carAge = CarAge;
    }

    public void setName(String Name)
    {
        name = Name;
    }

    public void setCarType(String CarType)
    {
        carType = CarType;
    }

    public void setCarAge(String CarAge)
    {
        carAge = CarAge;
    }

    public void setIsSelected(Boolean flag)
    {
        isSelected = flag;
    }

    public String getName()
    {
        return name;
    }

    public String getCarType()
    {
        return carType;
    }

    public String getCarAge()
    {
        return carAge;
    }

    public Boolean getIsSelected()
    {
        return isSelected;
    }
}

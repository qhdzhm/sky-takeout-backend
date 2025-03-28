package com.sky.mapper;

import com.sky.entity.Employee;
import com.sky.entity.Vehicle;
import com.sky.entity.VehicleDriver;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 车辆驾驶员关联Mapper
 */
@Mapper
public interface VehicleDriverMapper {
    
    /**
     * 新增车辆驾驶员关联
     */
    @Insert("insert into vehicle_driver(vehicle_id, employee_id, is_primary, create_time, update_time) values(#{vehicleId}, #{employeeId}, #{isPrimary}, now(), now())")
    void insert(VehicleDriver vehicleDriver);
    
    /**
     * 删除车辆驾驶员关联
     */
    @Delete("delete from vehicle_driver where id = #{id}")
    void deleteById(Long id);
    
    /**
     * 通过车辆ID获取驾驶员关联列表
     */
    @Select("select * from vehicle_driver where vehicle_id = #{vehicleId}")
    List<VehicleDriver> getByVehicleId(Long vehicleId);
    
    /**
     * 通过员工ID获取车辆关联列表
     */
    @Select("select * from vehicle_driver where employee_id = #{employeeId}")
    List<VehicleDriver> getByEmployeeId(Long employeeId);
    
    /**
     * 查询某车辆的驾驶员数量
     */
    @Select("select count(*) from vehicle_driver where vehicle_id = #{vehicleId}")
    Integer countDriversByVehicleId(Long vehicleId);
    
    /**
     * 通过车辆ID和员工ID获取关联记录
     */
    @Select("select * from vehicle_driver where vehicle_id = #{vehicleId} and employee_id = #{employeeId}")
    VehicleDriver getByVehicleIdAndEmployeeId(@Param("vehicleId") Long vehicleId, @Param("employeeId") Long employeeId);
    
    /**
     * 获取某车辆的所有驾驶员信息
     */
    @Select("select e.* from employee e join vehicle_driver vd on e.id = vd.employee_id where vd.vehicle_id = #{vehicleId}")
    List<Employee> getDriversByVehicleId(Long vehicleId);
    
    /**
     * 获取员工分配的所有车辆信息
     */
    @Select("select v.* from vehicles v join vehicle_driver vd on v.vehicle_id = vd.vehicle_id where vd.employee_id = #{employeeId}")
    List<Vehicle> getVehiclesByEmployeeId(Long employeeId);
} 
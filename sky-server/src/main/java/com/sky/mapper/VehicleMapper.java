package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.VehiclePageQueryDTO;
import com.sky.entity.Vehicle;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface VehicleMapper {

    /**
     * 分页查询车辆
     */
    Page<Vehicle> pageQuery(VehiclePageQueryDTO vehiclePageQueryDTO);

    /**
     * 根据ID查询车辆
     */
    @Select("SELECT * FROM vehicles WHERE vehicle_id = #{id}")
    Vehicle getById(Long id);

    /**
     * 新增车辆
     */
    @Insert("INSERT INTO vehicles (vehicle_type, license_plate, rego_expiry_date, inspection_due_date, " +
            "status, notes, max_drivers, location, seat_count, create_time, update_time) " +
            "VALUES (#{vehicleType}, #{licensePlate}, #{regoExpiryDate}, #{inspectionDueDate}, " +
            "#{status}, #{notes}, #{maxDrivers}, #{location}, #{seatCount}, #{createTime}, #{updateTime})")
    void insert(Vehicle vehicle);

    /**
     * 更新车辆信息
     */
    @Update("UPDATE vehicles SET vehicle_type = #{vehicleType}, license_plate = #{licensePlate}, " +
            "rego_expiry_date = #{regoExpiryDate}, inspection_due_date = #{inspectionDueDate}, " +
            "status = #{status}, notes = #{notes}, max_drivers = #{maxDrivers}, location = #{location}, " +
            "seat_count = #{seatCount}, update_time = #{updateTime} WHERE vehicle_id = #{vehicleId}")
    void update(Vehicle vehicle);

    /**
     * 根据ID删除车辆
     */
    @Delete("DELETE FROM vehicles WHERE vehicle_id = #{id}")
    void deleteById(Long id);

    /**
     * 根据车牌号查询车辆
     */
    @Select("SELECT * FROM vehicles WHERE license_plate = #{licensePlate}")
    Vehicle getByLicensePlate(String licensePlate);

    /**
     * 获取所有活跃的车辆
     */
    @Select("SELECT * FROM vehicles WHERE status = 1 ORDER BY vehicle_id")
    List<Vehicle> getAllActiveVehicles();

    // ===== Dashboard统计相关方法 =====
    
    /**
     * 获取车辆总数
     * @return 车辆总数
     */
    @Select("SELECT COUNT(*) FROM vehicles")
    Integer count();

    /**
     * 根据状态获取车辆数量
     * @param status 车辆状态
     * @return 车辆数量
     */
    @Select("SELECT COUNT(*) FROM vehicles WHERE status = #{status}")
    Integer countByStatus(String status);
}
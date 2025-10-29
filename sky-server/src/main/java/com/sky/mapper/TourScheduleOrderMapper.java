package com.sky.mapper;

import com.sky.entity.TourScheduleOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.List;

/**
 * 行程排序Mapper接口
 */
@Mapper
public interface TourScheduleOrderMapper {

    /**
     * 根据ID查询排团记录
     * @param id 排团记录ID
     * @return 排团记录
     */
    @Select("SELECT * FROM tour_schedule_order WHERE id = #{id}")
    TourScheduleOrder getById(Integer id);

    /**
     * 通过订单ID查询行程排序
     * @param bookingId 订单ID
     * @return 行程排序列表
     */
    List<TourScheduleOrder> getByBookingId(Integer bookingId);

    /**
     * 通过日期范围查询行程排序
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 行程排序列表
     */
    List<TourScheduleOrder> getByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * 插入行程排序（根据实际数据库表结构）
     * @param tourScheduleOrder 行程排序对象
     */
    @Insert("INSERT INTO tour_schedule_order (booking_id, day_number, tour_id, tour_type, tour_date, title, description, display_order, " +
            "order_number, adult_count, child_count, contact_person, contact_phone, pickup_location, dropoff_location, special_requests, luggage_count, " +
            "hotel_level, include_hotel, room_type, hotel_room_count, hotel_check_in_date, hotel_check_out_date, room_details, " +
            "flight_number, arrival_departure_time, arrival_landing_time, return_flight_number, departure_departure_time, " +
            "departure_landing_time, tour_start_date, tour_end_date, pickup_date, dropoff_date, passenger_contact, " +
            "itinerary_details, is_first_order, from_referral, referral_code, service_type, payment_status, total_price, user_id, agent_id, operator_id, booking_date, " +
            "group_size, status, tour_name, tour_location, is_extra_schedule, schedule_type, pickup_time, dropoff_time, group_type, group_size_limit, created_at, updated_at) " +
            "VALUES (#{bookingId}, #{dayNumber}, #{tourId}, #{tourType}, #{tourDate}, #{title}, #{description}, #{displayOrder}, " +
            "#{orderNumber}, #{adultCount}, #{childCount}, #{contactPerson}, #{contactPhone}, #{pickupLocation}, #{dropoffLocation}, #{specialRequests}, #{luggageCount}, " +
            "#{hotelLevel}, #{includeHotel}, #{roomType}, #{hotelRoomCount}, #{hotelCheckInDate}, #{hotelCheckOutDate}, #{roomDetails}, " +
            "#{flightNumber}, #{arrivalDepartureTime}, #{arrivalLandingTime}, #{returnFlightNumber}, #{departureDepartureTime}, " +
            "#{departureLandingTime}, #{tourStartDate}, #{tourEndDate}, #{pickupDate}, #{dropoffDate}, #{passengerContact}, " +
            "#{itineraryDetails}, #{isFirstOrder}, #{fromReferral}, #{referralCode}, #{serviceType}, #{paymentStatus}, #{totalPrice}, #{userId}, #{agentId}, #{operatorId}, #{bookingDate}, " +
            "#{groupSize}, #{status}, #{tourName}, #{tourLocation}, #{isExtraSchedule}, #{scheduleType}, #{pickupTime}, #{dropoffTime}, #{groupType}, #{groupSizeLimit}, #{createdAt}, #{updatedAt})")
    void insert(TourScheduleOrder tourScheduleOrder);

    /**
     * 批量插入行程排序（使用XML配置）
     * @param tourScheduleOrders 行程排序对象列表
     */
    void insertBatch(List<TourScheduleOrder> tourScheduleOrders);

    /**
     * 更新行程排序
     * @param tourScheduleOrder 行程排序对象
     */
    @Update("UPDATE tour_schedule_order SET " +
            "booking_id=#{bookingId}, day_number=#{dayNumber}, tour_id=#{tourId}, tour_type=#{tourType}, " +
            "tour_date=#{tourDate}, title=#{title}, description=#{description}, display_order=#{displayOrder}, " +
            "order_number=#{orderNumber}, adult_count=#{adultCount}, child_count=#{childCount}, " +
            "contact_person=#{contactPerson}, contact_phone=#{contactPhone}, pickup_location=#{pickupLocation}, " +
            "dropoff_location=#{dropoffLocation}, special_requests=#{specialRequests}, luggage_count=#{luggageCount}, " +
            "hotel_level=#{hotelLevel}, room_type=#{roomType}, hotel_room_count=#{hotelRoomCount}, " +
            "hotel_check_in_date=#{hotelCheckInDate}, hotel_check_out_date=#{hotelCheckOutDate}, room_details=#{roomDetails}, " +
            "flight_number=#{flightNumber}, arrival_departure_time=#{arrivalDepartureTime}, arrival_landing_time=#{arrivalLandingTime}, " +
            "return_flight_number=#{returnFlightNumber}, departure_departure_time=#{departureDepartureTime}, " +
            "departure_landing_time=#{departureLandingTime}, tour_start_date=#{tourStartDate}, tour_end_date=#{tourEndDate}, " +
            "pickup_date=#{pickupDate}, dropoff_date=#{dropoffDate}, passenger_contact=#{passengerContact}, " +
            "itinerary_details=#{itineraryDetails}, is_first_order=#{isFirstOrder}, from_referral=#{fromReferral}, " +
            "referral_code=#{referralCode}, service_type=#{serviceType}, payment_status=#{paymentStatus}, " +
            "total_price=#{totalPrice}, user_id=#{userId}, agent_id=#{agentId}, operator_id=#{operatorId}, " +
            "booking_date=#{bookingDate}, group_size=#{groupSize}, status=#{status}, tour_name=#{tourName}, " +
            "tour_location=#{tourLocation}, updated_at=#{updatedAt} " +
            "WHERE id=#{id}")
    void update(TourScheduleOrder tourScheduleOrder);

    /**
     * 通过订单ID删除行程排序
     * @param bookingId 订单ID
     */
    void deleteByBookingId(Integer bookingId);

    /**
     * 根据ID删除行程排序
     * @param id 行程排序ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM tour_schedule_order WHERE id = #{id}")
    int deleteById(Integer id);

    /**
     * 根据订单ID更新联系人信息
     * @param bookingId 订单ID
     * @param contactPerson 联系人姓名
     * @param contactPhone 联系人电话
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET contact_person=#{contactPerson}, contact_phone=#{contactPhone}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateContactInfoByBookingId(Integer bookingId, String contactPerson, String contactPhone);

    /**
     * 根据订单号搜索行程排序
     * @param orderNumber 订单号
     * @return 行程排序列表
     */
    @Select("SELECT * FROM tour_schedule_order WHERE order_number LIKE CONCAT('%', #{orderNumber}, '%') ORDER BY tour_date, display_order")
    List<TourScheduleOrder> getByOrderNumber(String orderNumber);

    /**
     * 根据联系人姓名搜索行程排序
     * @param contactPerson 联系人姓名
     * @return 行程排序列表
     */
    @Select("SELECT * FROM tour_schedule_order WHERE contact_person LIKE CONCAT('%', #{contactPerson}, '%') ORDER BY tour_date, display_order")
    List<TourScheduleOrder> getByContactPerson(String contactPerson);

    /**
     * 根据订单ID和酒店信息更新接送地点
     * @param bookingId 订单ID
     * @param hotelName 酒店名称
     * @param hotelAddress 酒店地址
     * @param tourStartDate 行程开始日期
     * @param tourEndDate 行程结束日期
     * @param checkInDate 酒店入住日期
     * @param checkOutDate 酒店退房日期
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET " +
            "pickup_location = CASE " +
            "  WHEN tour_date = #{tourStartDate} THEN pickup_location " +  // 第一天保持原接送地点
            "  WHEN tour_date >= #{checkInDate} THEN CONCAT(#{hotelName}, CASE WHEN #{hotelAddress} IS NOT NULL THEN CONCAT(' (', #{hotelAddress}, ')') ELSE '' END) " +  // 入住期间从酒店出发
            "  ELSE pickup_location " +
            "END, " +
            "dropoff_location = CASE " +
            "  WHEN tour_date = #{tourEndDate} THEN dropoff_location " +  // 最后一天保持原送达地点
            "  WHEN tour_date >= #{tourStartDate} AND tour_date <= #{checkOutDate} THEN CONCAT(#{hotelName}, CASE WHEN #{hotelAddress} IS NOT NULL THEN CONCAT(' (', #{hotelAddress}, ')') ELSE '' END) " +  // 从行程开始到退房日都送到酒店
            "  ELSE dropoff_location " +
            "END, " +
            "updated_at = NOW() " +
            "WHERE booking_id = #{bookingId} AND tour_date BETWEEN #{tourStartDate} AND #{tourEndDate}")
    int updatePickupDropoffLocationWithHotel(Integer bookingId, String hotelName, String hotelAddress, 
                                           LocalDate tourStartDate, LocalDate tourEndDate, 
                                           LocalDate checkInDate, LocalDate checkOutDate);

    /**
     * 根据订单ID清除酒店相关的接送地点信息
     * @param bookingId 订单ID
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET " +
            "pickup_location = CASE " +
            "  WHEN pickup_location LIKE CONCAT('%', (SELECT h.hotel_name FROM hotel_bookings hb " +
            "    LEFT JOIN hotels h ON hb.hotel_id = h.id WHERE hb.tour_booking_id = #{bookingId} LIMIT 1), '%') " +
            "  THEN NULL ELSE pickup_location END, " +
            "dropoff_location = CASE " +
            "  WHEN dropoff_location LIKE CONCAT('%', (SELECT h.hotel_name FROM hotel_bookings hb " +
            "    LEFT JOIN hotels h ON hb.hotel_id = h.id WHERE hb.tour_booking_id = #{bookingId} LIMIT 1), '%') " +
            "  THEN NULL ELSE dropoff_location END, " +
            "updated_at = NOW() " +
            "WHERE booking_id = #{bookingId}")
    int clearHotelPickupDropoffLocation(Integer bookingId);

    /**
     * 根据订单ID更新特殊要求信息
     * @param bookingId 订单ID
     * @param specialRequests 特殊要求
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET special_requests=#{specialRequests}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateSpecialRequestsByBookingId(Integer bookingId, String specialRequests);

    /**
     * 根据订单ID更新接送地点信息
     * @param bookingId 订单ID
     * @param pickupLocation 接送地点
     * @param dropoffLocation 送机地点
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET pickup_location=#{pickupLocation}, dropoff_location=#{dropoffLocation}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updatePickupDropoffByBookingId(Integer bookingId, String pickupLocation, String dropoffLocation);

    /**
     * 根据订单ID更新航班信息
     * @param bookingId 订单ID
     * @param flightNumber 航班号
     * @param returnFlightNumber 返程航班号
     * @param arrivalLandingTime 到达时间
     * @param departureDepartureTime 离开时间
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET " +
            "flight_number=#{flightNumber}, return_flight_number=#{returnFlightNumber}, " +
            "arrival_landing_time=#{arrivalLandingTime}, departure_departure_time=#{departureDepartureTime}, " +
            "updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateFlightInfoByBookingId(Integer bookingId, String flightNumber, String returnFlightNumber, 
                                   java.time.LocalDateTime arrivalLandingTime, java.time.LocalDateTime departureDepartureTime);

    /**
     * 根据订单ID更新联系人姓名
     * @param bookingId 订单ID
     * @param contactPerson 联系人姓名
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET contact_person=#{contactPerson}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateContactPersonByBookingId(Integer bookingId, String contactPerson);

    /**
     * 根据订单ID更新联系人电话
     * @param bookingId 订单ID
     * @param contactPhone 联系人电话
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET contact_phone=#{contactPhone}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateContactPhoneByBookingId(Integer bookingId, String contactPhone);

    /**
     * 根据订单ID更新乘客人数信息
     * @param bookingId 订单ID
     * @param adultCount 成人数量
     * @param childCount 儿童数量
     * @param groupSize 团队总人数
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET adult_count=#{adultCount}, child_count=#{childCount}, group_size=#{groupSize}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updatePassengerCountByBookingId(Integer bookingId, Integer adultCount, Integer childCount, Integer groupSize);

    /**
     * 根据ID更新导游备注
     * @param id 记录ID
     * @param guideRemarks 导游备注
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET guide_remarks=#{guideRemarks}, updated_at=NOW() WHERE id=#{id}")
    int updateGuideRemarksById(Integer id, String guideRemarks);

    /**
     * 根据订单ID更新导游备注
     * @param bookingId 订单ID
     * @param guideRemarks 导游备注
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET guide_remarks=#{guideRemarks}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateGuideRemarksByBookingId(Integer bookingId, String guideRemarks);

    /**
     * 根据ID更新特殊要求
     * @param id 记录ID
     * @param specialRequests 特殊要求
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET special_requests=#{specialRequests}, updated_at=NOW() WHERE id=#{id}")
    int updateSpecialRequestsById(Integer id, String specialRequests);

    /**
     * 更新第一天的接机地点为航班号
     * @param bookingId 订单ID
     * @param flightNumber 航班号
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET pickup_location = #{flightNumber}, updated_at = NOW() " +
            "WHERE booking_id = #{bookingId} AND day_number = 1")
    int updateFirstDayPickupLocation(@Param("bookingId") Integer bookingId, 
                                    @Param("flightNumber") String flightNumber);

    /**
     * 更新最后一天的送机地点为航班号
     * @param bookingId 订单ID
     * @param flightNumber 航班号
     * @return 更新的记录数
     */
    int updateLastDayDropoffLocation(@Param("bookingId") Integer bookingId, 
                                    @Param("flightNumber") String flightNumber);

    /**
     * 只更新第一天的接车地点
     * @param bookingId 订单ID
     * @param pickupLocation 接车地点
     * @return 更新的记录数
     */
    int updateFirstDayPickupLocationOnly(@Param("bookingId") Integer bookingId, 
                                        @Param("pickupLocation") String pickupLocation);

    /**
     * 只更新最后一天的送车地点
     * @param bookingId 订单ID
     * @param dropoffLocation 送车地点
     * @return 更新的记录数
     */
    int updateLastDayDropoffLocationOnly(@Param("bookingId") Integer bookingId, 
                                        @Param("dropoffLocation") String dropoffLocation);

    /**
     * 根据酒店名称和日期统计住在该酒店的所有客人
     * @param hotelName 酒店名称（模糊匹配）
     * @param tourDate 旅游日期
     * @return 客人统计列表
     */
    List<TourScheduleOrder> getCustomersByHotelAndDate(@Param("hotelName") String hotelName, 
                                                      @Param("tourDate") LocalDate tourDate);

    /**
     * 根据酒店名称、日期和导游获取客人信息
     * @param hotelName 酒店名称
     * @param tourDate 旅游日期  
     * @param guideName 导游姓名
     * @return 客人信息列表
     */
    List<TourScheduleOrder> getCustomersByHotelDateAndGuide(@Param("hotelName") String hotelName,
                                                           @Param("tourDate") LocalDate tourDate,
                                                           @Param("guideName") String guideName);

    // ==================== 乐观锁版本控制方法 ====================
    
    /**
     * 带版本控制的更新操作
     * @param tourScheduleOrder 排团记录（必须包含id和version）
     * @return 影响行数（0表示版本冲突）
     */
    int updateWithVersion(TourScheduleOrder tourScheduleOrder);

    /**
     * 带版本控制的批量更新操作  
     * @param tourScheduleOrders 排团记录列表
     * @return 影响行数
     */
    int updateBatchWithVersion(List<TourScheduleOrder> tourScheduleOrders);

    /**
     * 检查版本冲突
     * @param id 记录ID
     * @param version 预期版本号
     * @return 记录数量（1表示版本正确，0表示版本冲突）
     */
    int checkVersionConflict(@Param("id") Integer id, @Param("version") Integer version);

    /**
     * 获取最新版本信息
     * @param id 记录ID
     * @return 包含最新版本信息的记录
     */
    TourScheduleOrder getLatestVersionInfo(@Param("id") Integer id);
    
    /**
     * 根据订单ID查询所有记录（包含版本信息）
     * @param bookingId 订单ID
     * @return 排团记录列表
     */
    @Select("SELECT * FROM tour_schedule_order WHERE booking_id = #{bookingId} ORDER BY day_number, display_order")
    List<TourScheduleOrder> findByBookingId(@Param("bookingId") Long bookingId);

    /**
     * 更新酒店预订号到入住日期（第一次入住和换酒店的日期）
     * @param bookingId 订单ID
     * @param hotelBookingNumber 酒店预订号
     * @param checkInDates 入住日期列表
     */
    void updateHotelBookingNumberForCheckInDates(@Param("bookingId") Integer bookingId,
                                               @Param("hotelBookingNumber") String hotelBookingNumber,
                                               @Param("checkInDates") List<LocalDate> checkInDates);

    /**
     * 根据订单ID和日期更新游玩地点 - 用于同车订票拖拽功能
     * @param bookingId 订单ID
     * @param newLocation 新的游玩地点
     * @param tourDate 游玩日期
     * @return 影响的行数
     */
    @Update("UPDATE tour_schedule_order SET tour_location = #{newLocation}, title = #{newLocation}, updated_at = NOW() " +
            "WHERE booking_id = #{bookingId} AND tour_date = #{tourDate}")
    int updateTourLocationByBookingIdAndDate(@Param("bookingId") Integer bookingId,
                                           @Param("newLocation") String newLocation,
                                           @Param("tourDate") LocalDate tourDate);

    /**
     * 根据订单ID更新行程日期信息
     * @param bookingId 订单ID
     * @param tourStartDate 行程开始日期
     * @param tourEndDate 行程结束日期
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET tour_start_date=#{tourStartDate}, tour_end_date=#{tourEndDate}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateTourDatesByBookingId(Integer bookingId, LocalDate tourStartDate, LocalDate tourEndDate);

    /**
     * 根据订单ID更新酒店信息
     * @param bookingId 订单ID
     * @param hotelLevel 酒店等级
     * @param hotelRoomCount 房间数量
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET hotel_level=#{hotelLevel}, hotel_room_count=#{hotelRoomCount}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateHotelInfoByBookingId(Integer bookingId, String hotelLevel, Integer hotelRoomCount);

    /**
     * 根据订单ID更新房型信息
     * @param bookingId 订单ID
     * @param roomType 房型信息（JSON格式）
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET room_type=#{roomType}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateRoomTypeByBookingId(Integer bookingId, String roomType);

    /**
     * 根据订单ID更新订单状态信息
     * @param bookingId 订单ID
     * @param status 订单状态
     * @param paymentStatus 支付状态
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET status=#{status}, payment_status=#{paymentStatus}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateOrderStatusByBookingId(Integer bookingId, String status, String paymentStatus);

    /**
     * 根据订单ID更新总价信息
     * @param bookingId 订单ID
     * @param totalPrice 总价
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET total_price=#{totalPrice}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateTotalPriceByBookingId(Integer bookingId, java.math.BigDecimal totalPrice);

    /**
     * 根据订单ID重新生成tour_date - 基于新的tour_start_date和tour_end_date
     * @param bookingId 订单ID
     * @return 更新的记录数
     */
    int regenerateTourDatesByBookingId(Integer bookingId);

    /**
     * 根据订单ID删除超出指定天数的多余排团记录
     * @param bookingId 订单ID
     * @param maxDays 最大保留天数
     * @return 删除的记录数
     */
    @Delete("DELETE FROM tour_schedule_order WHERE booking_id = #{bookingId} AND day_number > #{maxDays}")
    int deleteExcessRecordsByBookingId(Integer bookingId, Integer maxDays);

    /**
     * 根据订单ID更新团型信息
     * @param bookingId 订单ID
     * @param groupType 团型类型
     * @param groupSizeLimit 团型人数限制
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET group_type=#{groupType}, group_size_limit=#{groupSizeLimit}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateGroupTypeByBookingId(Integer bookingId, String groupType, Integer groupSizeLimit);

    /**
     * 根据订单ID更新接送机日期信息
     * @param bookingId 订单ID
     * @param pickupDate 接机日期
     * @param dropoffDate 送机日期
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET pickup_date=#{pickupDate}, dropoff_date=#{dropoffDate}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updatePickupDropoffDatesByBookingId(Integer bookingId, LocalDate pickupDate, LocalDate dropoffDate);

    /**
     * 根据订单ID更新接送机时间信息
     * @param bookingId 订单ID
     * @param arrivalDepartureTime 接机时间
     * @param departureDepartureTime 送机时间
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET pickup_time=#{arrivalDepartureTime}, dropoff_time=#{departureDepartureTime}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updatePickupDropoffTimesByBookingId(Integer bookingId, String arrivalDepartureTime, String departureDepartureTime);

    /**
     * 根据订单ID更新酒店日期信息
     * @param bookingId 订单ID
     * @param hotelCheckInDate 酒店入住日期
     * @param hotelCheckOutDate 酒店退房日期
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET hotel_check_in_date=#{hotelCheckInDate}, hotel_check_out_date=#{hotelCheckOutDate}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateHotelDatesByBookingId(Integer bookingId, LocalDate hotelCheckInDate, LocalDate hotelCheckOutDate);
} 
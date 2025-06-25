package com.sky.mapper;

import com.sky.entity.TourScheduleOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
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
            "hotel_level, room_type, hotel_room_count, hotel_check_in_date, hotel_check_out_date, room_details, " +
            "flight_number, arrival_departure_time, arrival_landing_time, return_flight_number, departure_departure_time, " +
            "departure_landing_time, tour_start_date, tour_end_date, pickup_date, dropoff_date, passenger_contact, " +
            "itinerary_details, is_first_order, from_referral, referral_code, service_type, payment_status, total_price, user_id, agent_id, operator_id, booking_date, " +
            "group_size, status, tour_name, tour_location, created_at, updated_at) " +
            "VALUES (#{bookingId}, #{dayNumber}, #{tourId}, #{tourType}, #{tourDate}, #{title}, #{description}, #{displayOrder}, " +
            "#{orderNumber}, #{adultCount}, #{childCount}, #{contactPerson}, #{contactPhone}, #{pickupLocation}, #{dropoffLocation}, #{specialRequests}, #{luggageCount}, " +
            "#{hotelLevel}, #{roomType}, #{hotelRoomCount}, #{hotelCheckInDate}, #{hotelCheckOutDate}, #{roomDetails}, " +
            "#{flightNumber}, #{arrivalDepartureTime}, #{arrivalLandingTime}, #{returnFlightNumber}, #{departureDepartureTime}, " +
            "#{departureLandingTime}, #{tourStartDate}, #{tourEndDate}, #{pickupDate}, #{dropoffDate}, #{passengerContact}, " +
            "#{itineraryDetails}, #{isFirstOrder}, #{fromReferral}, #{referralCode}, #{serviceType}, #{paymentStatus}, #{totalPrice}, #{userId}, #{agentId}, #{operatorId}, #{bookingDate}, " +
            "#{groupSize}, #{status}, #{tourName}, #{tourLocation}, #{createdAt}, #{updatedAt})")
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
     * 根据订单ID更新联系人信息
     * @param bookingId 订单ID
     * @param contactPerson 联系人姓名
     * @param contactPhone 联系人电话
     * @return 更新的记录数
     */
    @Update("UPDATE tour_schedule_order SET contact_person=#{contactPerson}, contact_phone=#{contactPhone}, updated_at=NOW() WHERE booking_id=#{bookingId}")
    int updateContactInfoByBookingId(Integer bookingId, String contactPerson, String contactPhone);
} 
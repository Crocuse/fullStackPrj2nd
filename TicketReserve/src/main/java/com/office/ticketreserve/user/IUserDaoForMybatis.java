package com.office.ticketreserve.user;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.office.ticketreserve.productpage.PerfomanceDto;
import com.office.ticketreserve.reservation.ReservationDto;
import com.office.ticketreserve.review.ReviewDto;

@Mapper
public interface IUserDaoForMybatis {
	public boolean isUser(String u_id) ;
	public int insertUser(UserDto userDto);
	public UserDto selectUserForLogin(UserDto userDto);

	 public int updateUserInfo(UserDto userDto);
	 public UserDto getLatestUserInfo(String u_id);

	public UserDto selectUser(int u_no);
	public int deleteUser(int u_no) ;
	
	//예매 내역 불러오기 
	public List<ReservationDto> getMyTicketinfo(String u_id);
	public List<String>  getPerfomanceId(String tNo);
	public List<PerfomanceDto> getPerfomaceInfo(String p_id);
	//리뷰 가져오기
	public List<ReviewDto> getMyReviewInfo(String u_id);
	public String getPId(String p_name);	


}

package com.office.ticketreserve.user;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.office.ticketreserve.admin.AdminDaoForMyBatis;
import com.office.ticketreserve.admin.AdminDto;
import com.office.ticketreserve.productpage.PerfomanceDto;
import com.office.ticketreserve.reservation.ReservationDto;
import com.office.ticketreserve.review.ReviewDto;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class UserService {
	@Autowired
	IUserDaoForMybatis IUserDao;
	
	@Autowired
	UserDao userDao;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	@Autowired
	AdminDaoForMyBatis adminDaoForMyBatis;
	
	@Autowired
	JavaMailSenderImpl javaMailSenderImpl;
	
	@Autowired
	ReservationDto reservationDto;
	
	@Autowired
	PerfomanceDto perfomanceDto;
	
	final static public int ID_ALREADY_EXIST				= -2;
	final static public int DATABASE_COMMUNICATION_TROUBLE	= -1;
	final static public int INSERT_INFO_FAIL_AT_DATABASE	= 0;
	final static public int INSERT_INFO_OK_AT_DATABASE		= 1;
	
	
	// 회원가입 확인하기
	public int createAccountConfirm(UserDto userDto) {
		log.info("createAccountConfirm");
		
		boolean isMember = IUserDao.isUser(userDto.getU_id());
		
		if (!isMember) {
			
			AdminDto adminDtos= adminDaoForMyBatis.selectAdminById(userDto.getU_id());
			if(adminDtos != null) {
				return ID_ALREADY_EXIST;
			}
			int result = IUserDao.insertUser(userDto);
			
			if (result == DATABASE_COMMUNICATION_TROUBLE) {
				log.info("DATABASE COMMUNICATION TROUBLE");
			} else if (result == INSERT_INFO_FAIL_AT_DATABASE) {
				log.info("INSERT INFO FAIL AT DATABASE");
			} else if (result == INSERT_INFO_OK_AT_DATABASE) {
				log.info("INSERT INFO OK AT DATABASE");
			}
				
			return result;
		} else {
			return ID_ALREADY_EXIST;
		}
		
	}
	
	//아이디 중복확인
	public boolean checkId(String u_id_check) {
		log.info("[UserService] checkId()");
		
		boolean result = IUserDao.isUser(u_id_check);
		if(!result) {
			AdminDto adminDtos= adminDaoForMyBatis.selectAdminById(u_id_check);
			if(adminDtos != null) {
				result = true;
				return result;
			}
		}
		
        return result;
    }



	//유저와 관리자 로그인 확인
	public Object userLoginConfirm(UserDto userDto) {
	    log.info("[UserService] userLoginConfirm()");
	    
	   // UserDto seletedUserDto= userDao.selectUserForLogin(userDto);
	    UserDto seletedUserDto = IUserDao.selectUserForLogin(userDto);
	    
	    if(seletedUserDto != null) {
	        return seletedUserDto;
	    } 

	    AdminDto adminDto = adminDaoForMyBatis.selectAdminById(userDto.getU_id());
	    
	    if(adminDto != null && passwordEncoder.matches(userDto.getU_pw(), adminDto.getA_pw())) {
	    	return adminDto;
	    }
	        
	    return null;
	}


	// 정보수정 확인
	public UserDto userModifyConfirm(UserDto userDto) {
		log.info("[UserService] userModifyConfirm()");
		
		int result =userDao.editUserInfo(userDto);
		//int result = IUserDao.editUserInfo(userDto);
		
		if(result > 0)
			return  userDao.getLatestUserInfo(userDto); //IUserDao.getLatestUserInfo(userDto);
		else
			return null;
	}

	
	// 회원 탈퇴 확인
	public int userDeleteConfirm(int u_no,UserDto userDto) {
		log.info("[UserService] userLoginConfirm()");
		String u_pw = userDto.getU_pw();
		int result = -1;
		
		UserDto userDtos = IUserDao.selectUser(u_no);
		if(userDtos != null && 
				passwordEncoder.matches(u_pw, userDtos.getU_pw())) {	//사용자입력값과 DB 비교
			
			result = IUserDao.deleteUser(u_no);
			
			if(result <= 0) {
				result = -1;
			} else {
				result = 1;
			}
		}
		
		 return result;
	}
	
	
	//비밀번호 찾기 확인
	public int uFindPwConfirm(UserDto userDto) {
		log.info("[UserService] userLoginConfirm()");
		
		UserDto userDtos = userDao.seletUserFindInfo(userDto.getU_id(), userDto.getU_mail());
		
		int result = 0;
		
		if(userDtos != null) {
			
			String newPassword = createNewPassword();
			
			result = userDao.updatePassword(userDto.getU_id(), newPassword);
			
			if (result > 0)
				
				sendNewPasswordByMail(userDto.getU_mail(), newPassword);
			
		}
		
			return result;
	}
	
	
	//비밀번호 찾기 메일 전송하기
	private void sendNewPasswordByMail(String toMailAddr , String newPassword) {
		log.info("[UserService] sendNewPasswordByMail()");
		MimeMessagePreparator mimeMessagePreparator = new MimeMessagePreparator() {
			
			@Override
			public void prepare(MimeMessage mimeMessage) throws Exception {
				
				MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
				mimeMessageHelper.setTo(toMailAddr);
				mimeMessageHelper.setSubject("[TickeTi] 새 비밀번호 안내입니다.");
				String emailBody = "새 비밀번호: " + newPassword +  "\n로그인 후 보안을 위해 비밀번호를 변경해주세요.";
				mimeMessageHelper.setText(emailBody, false);
			}
		};
		
		javaMailSenderImpl.send(mimeMessagePreparator);
		return;
    }
		

	// 새로운 비밀번호 생성
	private String createNewPassword() {
		log.info("[UserService] createNewPassword()");
		
		char[] chars = new char[] {
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 
				'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 
				'u', 'v', 'w', 'x', 'y', 'z'
				};
		
		StringBuffer stringBuffer = new StringBuffer();
		SecureRandom secureRandom = new SecureRandom();
		secureRandom.setSeed(new Date().getTime());
		
		int index = 0;
		int length = chars.length;
		for (int i = 0; i < 6; i++) {
			index = secureRandom.nextInt(length);
			
			if (index % 2 == 0)
				stringBuffer.append(String.valueOf(chars[index]).toUpperCase());
			else
				stringBuffer.append(String.valueOf(chars[index]).toLowerCase());
			
		}
				
		return stringBuffer.toString();
		
	}
	
	//아이디 찾기 
	public String dofindId(UserDto userDto, Model model) {
		log.info("[UserService] dofindId()");
		
		  String userId = userDao.dofindIdFromDB(userDto.getU_name(), userDto.getU_mail());
		    
		    if (userId != null) {
		        model.addAttribute("userId", userId);
		    }
		   
		    return userId;
	}
	
	// 비밀번호 변경하기 
	public int modifyFwConfirm(String newPassword, UserDto loginedUserDto, UserDto userDto) {
		log.info("[UserService] modifyFwConfirm()");
		
		int u_no = loginedUserDto.getU_no();
		String u_id = loginedUserDto.getU_id();
		String current_pw = userDto.getU_pw();
		int result = -1;
		UserDto userDtos = IUserDao.selectUser(u_no);
		
		if(userDtos != null && 
				passwordEncoder.matches(current_pw, userDtos.getU_pw())) {	//사용자입력값과 DB 비교
//			String u_pw = passwordEncoder.encode(newPassword);
			result = userDao.updatePassword(u_id, newPassword);
			
			if(result > 0) {
				result = 1;
			} 
		}
		return result;
	
	}
	
	// 나의 예매 내역
	public Map<String, Object> getMyTicketInfo(String u_id) {
		log.info("[UserService] getMyTicketInfo()");
		
		Map<String, Object> combinedInfo = new HashMap<>();
		List<ReservationDto> myReservationDto  = new ArrayList<>();
		
		myReservationDto = IUserDao.getMyTicketinfo(u_id);
		 if (myReservationDto != null && !myReservationDto.isEmpty()) {
			 
		// t_no , view에서 보여줄 내역 다 뽑아내기
		List<String> r_reg_date_colection = new ArrayList<>();
		List<String> t_seat_colection = new ArrayList<>();
		List<String> r_date_colection = new ArrayList<>();
		List<String> r_time_colection = new ArrayList<>();
		List<String> r_take_ticket_colection = new ArrayList<>();
		List<String> r_payment_state_colection = new ArrayList<>();
		List<String> ticket_numbers = new ArrayList<>();
		List<String> r_no_colection = new ArrayList<>();
		int r_take_ticket_result = 1;
		int r_payment_result = 0;
		
		for(ReservationDto reservationDto : myReservationDto) {
			ticket_numbers.add(Integer.toString(reservationDto.getT_no()));
			r_reg_date_colection.add(reservationDto.getR_reg_date()); 
			t_seat_colection.add(reservationDto.getT_seat());
			r_date_colection.add(reservationDto.getR_date());
			r_time_colection.add(reservationDto.getR_time());
			r_take_ticket_result = reservationDto.getR_take_ticket(); 
			r_no_colection.add(Integer.toString(reservationDto.getR_no()));
			
			
			if(r_take_ticket_result == 1) {
				r_take_ticket_colection.add("현장 수령");
				
			} else if(r_take_ticket_result == 2) {
				r_take_ticket_colection.add("메일 발송");
				
			} else if(r_take_ticket_result == 3) {
				r_take_ticket_colection.add("문자 발송");
				
			} else {
				r_take_ticket_colection.add("오류");
				
			}
			
			r_payment_result = reservationDto.getR_payment_state();
			if(r_payment_result == 0) {
				r_payment_state_colection.add("미결제");
				
			} else if(r_payment_result == 1) {
				r_payment_state_colection.add("결제 완료");
				
			} else if(r_payment_result == -1) {
				r_payment_state_colection.add("결제 취소");
				
			} else {
				r_payment_state_colection.add("오류");
				
			}
		}
		
		//p_id 얻어오기
		List<String> myPIdsList = new ArrayList<>();
		 for (String tNo :ticket_numbers) { 
			 
			List<String> myPIds = IUserDao.getPerfomanceId(tNo); 
				myPIdsList.addAll(myPIds);
			 }
		 
		 //perfomanceDto 얻어오기
		 List<PerfomanceDto> my_perfomance_Dto = new ArrayList<>();
		 List<String> p_name_colection = new ArrayList<>();
		 List<String> p_thum_colectiion = new ArrayList<>();
		 for(String p_id : myPIdsList) {
			 
			 my_perfomance_Dto = IUserDao.getPerfomaceInfo(p_id);
			 for(PerfomanceDto perfomanceDto : my_perfomance_Dto) {
				 p_name_colection.add(perfomanceDto.getP_name());
				 p_thum_colectiion.add(perfomanceDto.getP_thum());
			 }
		 }
		 
		combinedInfo.put("r_reg_date_colection", r_reg_date_colection);
		combinedInfo.put("t_seat_colection", t_seat_colection);
		combinedInfo.put("r_date_colection", r_date_colection);
		combinedInfo.put("r_time_colection", r_time_colection);
		combinedInfo.put("r_take_ticket_colection", r_take_ticket_colection);
		combinedInfo.put("r_payment_state_colection", r_payment_state_colection);
		combinedInfo.put("p_name_colection", p_name_colection);
		combinedInfo.put("p_thum_colectiion", p_thum_colectiion);
		combinedInfo.put("myPIdsList", myPIdsList);
		combinedInfo.put("ticket_numbers", ticket_numbers);
		combinedInfo.put("r_no_colection", r_no_colection);
		
		}
		return combinedInfo;
	}
	
	// 나의 리뷰내역 가져오기
	public Map<String, Object> getMyReview(String u_id) {
		log.info("[UserService] getMyReview()");
		
		Map<String, Object> combinedRvInfo = new HashMap<>();
		List<ReviewDto> rvInfo = IUserDao.getMyReviewInfo(u_id);
		
		if (rvInfo != null && !rvInfo.isEmpty()) {
		
		List<String>rv_no_colection = new ArrayList<>();
		List<String>p_name_colection = new ArrayList<>();
		List<String>rv_txt_colection = new ArrayList<>();
		List<String>rv_score_colection = new ArrayList<>();
		List<String>rv_reg_date_colection = new ArrayList<>();
		List<String>p_names = new ArrayList<>();
		List<String>p_ids = new ArrayList<>();
		
		for(ReviewDto rv_infomation : rvInfo) {
			
			 if (rv_infomation.getRv_txt() == null && rv_infomation.getRv_txt().isEmpty()) {
				 // 후기가 없는 경우 
			        return null;
			        
			    }else {
			    	p_names.add(rv_infomation.getP_name());
			    	
			    	for(String p_name :p_names) {
			    		
			    		p_ids.add(IUserDao.getPId(p_name));
			    	}
			    	rv_no_colection.add(Integer.toString(rv_infomation.getRv_no()));
			    	p_name_colection.add(rv_infomation.getP_name());
					rv_txt_colection.add(rv_infomation.getRv_txt());
					rv_score_colection.add(Integer.toString(rv_infomation.getRv_score()));
					rv_reg_date_colection.add(rv_infomation.getRv_reg_date());
			    }
			 
		}
			combinedRvInfo.put("rv_no_colection", rv_no_colection);
			combinedRvInfo.put("p_name_colection", p_name_colection);
			combinedRvInfo.put("rv_txt_colection", rv_txt_colection);
			combinedRvInfo.put("rv_score_colection", rv_score_colection);
			combinedRvInfo.put("rv_reg_date_colection", rv_reg_date_colection);
			combinedRvInfo.put("p_ids", p_ids);
		}
		
		return combinedRvInfo;
	}
}

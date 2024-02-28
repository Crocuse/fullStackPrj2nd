package com.office.worldcup.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.extern.log4j.Log4j2;

@Controller
@RequestMapping("/user")
@Log4j2
public class UserController {

	@Autowired
	UserService userService;
	
	// 유저 회원가입폼이동
	@GetMapping("/create_account_form")
	public String createAccountForm() {
		log.info("createAccountForm()");
		
		String nextPage = "user/create_account_form";
		
		return nextPage;
	}
	
	
	// 유저 회원가입 확인
	@PostMapping("/create_account_confirm")
	public String createAccountConfirm(UserDto userDto) {
		log.info("createAccountConfirm()");
		
		String nextPage = "user/create_account_ok";
		
		int result = userService.createAccountConfirm(userDto);
		
		if (result <= UserService.INSERT_INFO_FAIL_AT_DATABASE) {
			nextPage = "user/create_account_ng";
		}
		
		return nextPage;
		
	}
	// 유저 로그인폼 이동
	@GetMapping("/user_login_form")
	public String userLoginForm() {
		log.info("userLoginForm()");
      
		String nextPage = "user/user_login_form";
      
		return nextPage;   
      
	}
	// 유저 로그인 확인
	@PostMapping("/user_login_confirm")
	public String userLoginConfirm(UserDto userDto, HttpSession session) {
		log.info("userLoginConfirm()");
	      
		String nextPage = "user/user_login_ok";
	      
		UserDto loginedUserDto = userService.userLoginConfirm(userDto);
	    
		if (loginedUserDto != null) {
			
			session.setAttribute("loginedUserDto", loginedUserDto);
			session.setMaxInactiveInterval(60 * 30);
			
		} else {
			nextPage = "user/user_login_ng";
			
		}
		
		return nextPage;
	}
	// 유저 정보수정폼 이동
	@GetMapping("/user_modify_form")
	public String userModifyForm() {
		log.info("userModifyForm()");
	      
		String nextPage = "user/user_modify_form";
	      
		return nextPage;
	}
	//유저 정보수정 확인
	@PostMapping("/user_modify_confirm")
	public String userModifyConfirm(UserDto userDto, HttpSession session) {
	   log.info("userModifyConfirm()");
	    
	   String nextPage = "user/user_modify_ok";
	   
	   UserDto loginedUserDto = userService.userModifyConfirm(userDto);
	    
	    if (loginedUserDto != null) {
	       
	       session.setAttribute("loginedUserDto", loginedUserDto);
	       session.setMaxInactiveInterval(60 * 30);
	   
	    } else {
	       
	       nextPage = "user/user_modify_ng";
	       
	    }
	    
	    return nextPage;
	    
	}
	
	/*
	 * 유저 로그아웃
	 */
	@GetMapping("/user_logout_confirm")
    public String userLogoutConfirm(HttpSession session) {
		log.info("userLogoutConfirm()");

		String nextPage = "redirect:/";
		
		session.removeAttribute("loginedUserDto");
		
		return nextPage;
		
    }
	/*
	 * 유저 회원탈퇴
	 */
	@GetMapping("/user_delete_confirm")
	public String userDeleteConfirm(HttpSession session) {
		log.info("userDeleteConfirm()");
      
		String nextPage = "redirect:/user/user_logout_confirm";

		UserDto loginedUserDto =
				(UserDto) session.getAttribute("loginedUserDto");
		
		int result = userService.memberDeleteConfirm(loginedUserDto.getU_no());
		if (result <= 0) {
	         nextPage = "user/user_delete_ng";
		} 
      
		return nextPage;
	}
	//뭐징
}
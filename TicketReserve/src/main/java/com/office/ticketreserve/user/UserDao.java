package com.office.ticketreserve.user;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Repository
public class UserDao {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Autowired
	PasswordEncoder passwordEncoder;
	
	public boolean isUser(String u_id) {
		log.info("[UserDao] isUser()");
		
		String sql= "SELECT COUNT(*) FROM TBL_USER WHERE U_ID = ? ";
		
		int result = -1;
		
		try {
			
			result = jdbcTemplate.queryForObject(sql,Integer.class,u_id);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return result > 0 ? true : false;
	}

	public int insertUser(UserDto userDto) {
		
		String sql= "INSERT INTO "
					+ "TBL_USER("
							+ "U_NO,"
							+ "U_ID,"
							+ "U_PW,"
							+ "U_NAME,"
							+ "U_MAIL,"
							+ "U_PHONE,"
							+ "U_SC_NUM,"
							+ "U_ADDRESS,"
							+ "U_REG_DATE,"
							+ "U_MOD_DATE) "
					+ "VALUES ("
							+ "USER_SEQ.NEXTVAL,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)";
		int result =-1;
		try {
			result= jdbcTemplate.update(sql,
											userDto.getU_id(),
											passwordEncoder.encode(userDto.getU_pw()),
											userDto.getU_name(),
											userDto.getU_mail(),
											userDto.getU_phone(),
											userDto.getU_sc_num(),
											userDto.getU_address());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}

	public UserDto selectUserForLogin(UserDto userDto) {
		log.info("[UserDao] selectUserForLogin()");
		
		String sql = "SELECT * FROM TBL_USER WHERE U_ID = ?";
		
		List<UserDto> userDtos = new ArrayList<UserDto>();
        
		try {
			
			RowMapper<UserDto> rowMapper = 
					BeanPropertyRowMapper.newInstance(UserDto.class);
			userDtos = jdbcTemplate.query(sql, rowMapper, userDto.getU_id());
			
			if (userDtos.size() <= 0) {
				return null;
				
			} else {
				
				if (!passwordEncoder.matches(userDto.getU_pw(), userDtos.get(0).getU_pw())) {
					return null;
				} 
			}

		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return userDtos.get(0);
		 
	}

	public List<UserDto> selectAllUsers() {
		log.info("[UserDao] selectAllUsers()");
		
		String sql = "SELECT * FROM TBL_USER";
		
		List<UserDto> userDtos = new ArrayList<UserDto>();
        
		try {
			RowMapper<UserDto> rowMapper = 
					BeanPropertyRowMapper.newInstance(UserDto.class);
			userDtos = jdbcTemplate.query(sql, rowMapper);

			if (userDtos.isEmpty()) {
				return null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			
		}
		
		return userDtos;
	}

	public static UserDto selectAdminById(String u_id_check) {
		log.info("[UserDao] selectAdminById()");
		
		return null;
	}
	
	
	// 회원 탈퇴
	public int deleteUser(int u_no) {
		log.info("[UserDao] deleteUser()");
		
		String sql =  "DELETE FROM TBL_USER "
				+ "WHERE U_NO = ?";
	
	int result = -1;
	
	try {
		
		result = jdbcTemplate.update(sql, u_no);
		
	} catch (Exception e) {
		e.printStackTrace();
		
	}
	
	return result;
	}

	// 정보 수정
	public int editUserInfo(UserDto userDto) {
		log.info("[UserDao] deleteUser()");

		String sql =  "UPDATE "
                + "TBL_USER "
                + "SET "
                + "U_NAME = ?, "
                + "U_MAIL = ?, "
                + "U_PHONE = ?, "
                + "U_ADDRESS = ?, "
                + "U_MOD_DATE = CURRENT_TIMESTAMP "
                + "WHERE "
                + "U_NO = ?";
	
	int result = -1;
	
	try {
		
		result = jdbcTemplate.update(sql,
										userDto.getU_name(),
										userDto.getU_mail(),
										userDto.getU_phone(),
										userDto.getU_address(),
										userDto.getU_no());
				
	} catch (Exception e) {
		e.printStackTrace();
		
	}
	
	return result;
	
	}
	
	// 수정된 최신 정보 불러오기
	public UserDto getLatestUserInfo(UserDto userDto) {
		log.info("[UserDao] getLatestUserInfo()");
		
		String sql = "SELECT * FROM TBL_USER WHERE U_NO = ?";
		List<UserDto> UserDtos = new ArrayList<>();
		
		try {
			
			RowMapper<UserDto> rowMapper =
					BeanPropertyRowMapper.newInstance(UserDto.class);
			
			UserDtos = jdbcTemplate.query(sql, rowMapper, userDto.getU_no());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return UserDtos.size() > 0 ? UserDtos.get(0) : null;	
	}

	public static UserDto selectUserById(String u_id_check) {
		log.info("[UserDao] getLatestUserInfo()");
		
		return null;
	}

	//비밀번호 찾기 회원 유무
	public UserDto seletUserFindInfo(String u_id, String u_mail) {
		log.info("[UserDao] seletUserFindInfo()");
		
		String sql = "SELECT * FROM TBL_USER WHERE U_ID = :1 AND U_MAIL = :2";
		
		List<UserDto> userDtos = new ArrayList<>();
		
		try {
			
			RowMapper<UserDto> rowMapper =
					BeanPropertyRowMapper.newInstance(UserDto.class);
			
			userDtos = jdbcTemplate.query(sql, rowMapper, u_id , u_mail);
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return userDtos.size() > 0 ? userDtos.get(0) : null ;
		
	}

	public int updatePassword(String u_id, String newPassword) {
		log.info("[UserDao] updatePassword()");
		
		String sql = "UPDATE TBL_USER "
				+ "SET "
				+ "U_PW = ?, U_MOD_DATE = CURRENT_TIMESTAMP "
				+ "WHERE U_ID = ?";
		
		int result = -1;
		
		try {
			
			result= jdbcTemplate.update(sql,
											passwordEncoder.encode(newPassword),
											u_id);
			
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return result;
	}
	
	  public String dofindIdFromDB(String u_name, String u_mail) {
		  log.info("[UserDao] dofindIdFromDB()");
		  
		  String sql = "SELECT U_ID FROM TBL_USER WHERE U_NAME = :1 AND U_MAIL = :2";
		
		  try {
		  
		  List<String> userIds = jdbcTemplate.queryForList(sql, String.class, u_name, u_mail);
		  
		  if (!userIds.isEmpty()) {
	            return userIds.get(0);
		  }
		  
		  } catch (Exception e) { e.printStackTrace(); } 
		  
		  return null;
	  
	  }
	 

}

package com.toko.osprey.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.toko.osprey.dao.UserRepo;
import com.toko.osprey.entity.User;
import com.toko.osprey.util.EmailUtil;

@RestController
@CrossOrigin
@RequestMapping("/users")
public class UserController {
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private EmailUtil emailUtil;
	private PasswordEncoder pwEncoder =  new BCryptPasswordEncoder();
	
	@GetMapping("/semuaUser")
	public List<User> getAllUser() {
		return userRepo.findAll();
	}
	
	@GetMapping("/id/{userId}")
	public Optional<User> showUser(@PathVariable int userId) {
		return userRepo.findById(userId);
	}
	//edit Profile
	@PutMapping("/editProfile")
	public User editProfile(@RequestBody User user){
		User findUsername = userRepo.findById(user.getId()).get();
		String namaSementara = findUsername.getUsername();
		String emailSementara = findUsername.getEmail();
		findUsername.setUsername(null);
		findUsername.setEmail(null);
		userRepo.save(findUsername);
//		System.out.println(findUsername.getEmail());
		Optional<User> usernameUsed = userRepo.findByUsername(user.getUsername());
		if (usernameUsed.toString()!= "Optional.empty") {	
			findUsername.setUsername(namaSementara);
			findUsername.setEmail(emailSementara);
			userRepo.save(findUsername);
			throw new RuntimeException("Username Exist!");
		}
		Optional<User> findEmail = userRepo.findByEmail(user.getEmail());
		if (findEmail.toString()!= "Optional.empty") {	
			findUsername.setUsername(namaSementara);
			findUsername.setEmail(emailSementara);
			userRepo.save(findUsername);
			throw new RuntimeException("Email Exist!");
		}
		findUsername.setUsername(user.getUsername());
		findUsername.setEmail(user.getEmail());
		userRepo.save(findUsername);
		user.setId(findUsername.getId());
		user.setVerified(findUsername.isVerified());
		user.setRole(findUsername.getRole());
		user.setVerifyToken(findUsername.getVerifyToken());
		user.setPassword(findUsername.getPassword());
		User savedUser = userRepo.save(user);
		savedUser.setPassword(null);
//		System.out.println(findUsername);
		return savedUser;
	}
	//edit password
	@GetMapping("/password/{userId}/{oldPassword}/{newPassword}")
	public User passwordEdit(@PathVariable int userId, @PathVariable String oldPassword, @PathVariable String newPassword){
		User findUser = userRepo.findById(userId).get();
		if (!findUser.isVerified()) {
			throw new RuntimeException("Akun Belum Terverifikasi");
		}
		if (pwEncoder.matches(oldPassword, findUser.getPassword())) {
			String encodedPassword = pwEncoder.encode(newPassword);
			findUser.setPassword(encodedPassword);
			User savedUser = userRepo.save(findUser);
			savedUser.setPassword(null);
			return savedUser;
		}
		System.out.println(oldPassword);
		throw new RuntimeException("Old Password Not Match");
	}
	
	//kirim Email lupa password
	@GetMapping("/forgetPass/{username}")
	public User editPassword(@PathVariable String username) {
		Optional<User> findUsername = userRepo.findByUsername(username);
		if (findUsername.toString() == "Optional.empty") 	
			 throw new RuntimeException("Username doesn't Exist!");	
			String verifyToken = pwEncoder.encode(findUsername.get().getUsername() + findUsername.get().getEmail());
			System.out.println(verifyToken);
			String message = "klik link ini untuk ganti password "+ "http://localhost:3000/LupaPassword/" + findUsername.get().getUsername()+"/"+ verifyToken.substring(15, 20);	
			emailUtil.sendEmail(findUsername.get().getEmail(), "Verifikasi Ganti Password", message);
			return findUsername.get();
	}
	
	@GetMapping("/email/verify/{username}")
	public String verifyDiProfile(@PathVariable String username) {
		User findUser = userRepo.findByUsername(username).get();
		String linkToVerify = "http://localhost:8080/users/verify/" + findUser.getUsername() + "?token=" + findUser.getVerifyToken();
		
		String message = "<h1>Verify Email !</h1>\n";
		message += "Akun dengan username " + findUser.getUsername() + " telah terdaftar!\n";
		message += "Klik <a href=\"" + linkToVerify + "\">link ini</a> untuk verifikasi email anda.";
		
		
		emailUtil.sendEmail(findUser.getEmail(), "Registrasi Akun", message);
		return "Silahkan Check Email";
	}
//	ganti password
//	@patch("/editLupaPassword")
	@PutMapping("/editLupaPassword")
	public User editLupaPassword(@RequestBody User user) {
		System.out.println(user.getPassword());
		System.out.println(user.getUsername());
		User findUsername = userRepo.findByUsername(user.getUsername()).get();
		user.setId(findUsername.getId());
		user.setAlamat(findUsername.getAlamat());
		user.setEmail(findUsername.getEmail());
		user.setFullName(findUsername.getFullName());
		user.setVerified(findUsername.isVerified());
		user.setNoHp(findUsername.getNoHp());
		user.setRole(findUsername.getRole());
		user.setUsername(findUsername.getUsername());
		user.setVerifyToken(findUsername.getVerifyToken());
		System.out.println(findUsername.getUsername());
		String encodedPassword = pwEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		User savedUser = userRepo.save(user);
		savedUser.setPassword(null);
		return savedUser;
	}
	//register
	@PostMapping
	public User addUser(@RequestBody User user) {
		if (user.getUsername().equals("")) {
			throw new RuntimeException("Username Kosong");
		}
		else if (user.getEmail().equals("")) {
			throw new RuntimeException("Email Kosong");
		}
		else if (user.getPassword().equals("")) {
			throw new RuntimeException("Password Kosong");
		}
		
		Optional<User> findUsername = userRepo.findByUsername(user.getUsername());
		Optional<User> findEmail = userRepo.findByEmail(user.getEmail());
		if (findUsername.toString()!= "Optional.empty") 	
			 throw new RuntimeException("Username Exist!");
		
		if (findEmail.toString()!= "Optional.empty") 
			throw new RuntimeException("Email Exist!");
		String encodedPassword = pwEncoder.encode(user.getPassword());
		String verifyToken = pwEncoder.encode(user.getUsername() + user.getEmail());
		
		user.setPassword(encodedPassword);
		user.setVerified(false);
		// Simpan verifyToken di database
		user.setVerifyToken(verifyToken);
		user.setRole("user");
		User savedUser = userRepo.save(user);
		savedUser.setPassword(null);
		
		// Kirim verifyToken si user ke emailnya user
		String linkToVerify = "http://localhost:8080/users/verify/" + user.getUsername() + "?token=" + verifyToken;
		
		String message = "<h1>Selamat! Registrasi Berhasil</h1>\n";
		message += "Akun dengan username " + user.getUsername() + " telah terdaftar!\n";
		message += "Klik <a href=\"" + linkToVerify + "\">link ini</a> untuk verifikasi email anda.";
		
		
		emailUtil.sendEmail(user.getEmail(), "Registrasi Akun", message);
		
		return savedUser;
	}
	@GetMapping("/verify/{username}")
	public String verifyUserEmail (@PathVariable String username, @RequestParam String token) {
		User findUser = userRepo.findByUsername(username).get();
		
		if (findUser.getVerifyToken().equals(token)) {
			findUser.setVerified(true);
		} else {
			throw new RuntimeException("Token is invalid");
		}
		
		userRepo.save(findUser);
		
		return "Sukses!";
	}
	
	@PostMapping("/login")
	public User loginUser(@RequestBody User user) {
		Optional<User> findUser = userRepo.findByUsername(user.getUsername());
							// Password raw/sblm encode  |  password sdh encode
		if (findUser.toString()!= "Optional.empty") {			
			if(pwEncoder.matches(user.getPassword(), findUser.get().getPassword())) {
				findUser.get().setPassword(null);
				return findUser.get();
			}else {
				throw new RuntimeException("Wrong Password");
			}
		}
		throw new RuntimeException("Username doesn't  Exist!");
	}
	@PostMapping("/login/{id}")
	public User userKeepLogin(@PathVariable int id) {
		User findUser = userRepo.findById(id).get();
		if (findUser.toString()!= "Optional.empty") {			
			return findUser;
		}
		throw new RuntimeException("Username doesn't Exist!");
	}
}

package com.toko.osprey.controller;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toko.osprey.dao.PaketRepo;
import com.toko.osprey.dao.ProductRepo;
import com.toko.osprey.dao.TransactionsDetailsRepo;
import com.toko.osprey.dao.TransactionsRepo;
import com.toko.osprey.dao.UserRepo;
import com.toko.osprey.entity.Product;
import com.toko.osprey.entity.Transactions;
import com.toko.osprey.entity.User;
import com.toko.osprey.util.EmailUtil;

@RestController
@CrossOrigin
@RequestMapping("/transactions")
public class TransactionsController {

	@Autowired
	private TransactionsRepo transactionsRepo;
	@Autowired
	private TransactionsDetailsRepo transactionsDetailsRepo;
	@Autowired
	private UserRepo userRepo;
	@Autowired
	private ProductRepo productRepo;
	@Autowired
	private PaketRepo paketRepo;
	
	@Autowired
	private EmailUtil emailUtil;
	private PasswordEncoder pwEncoder =  new BCryptPasswordEncoder();
	
	private String uploadPath = System.getProperty("user.dir") + "\\src\\main\\resources\\static\\images\\";
	
	@GetMapping
	public Iterable<Transactions> getAllTransactions(){
		return transactionsRepo.findAll();
	}
	
	@PostMapping("/addToTransactions/{userId}")
	public Transactions addToTransactions(@PathVariable int userId, @RequestBody Transactions transactions) {
		User findUser = userRepo.findById(userId).get();
		transactions.setUser(findUser);
		transactions.setStatusPengiriman("Belum Dikirim");
		return transactionsRepo.save(transactions);
	}
	
	@GetMapping("/user/{userId}")
	public Iterable<Transactions> getTransactionsByUser(@PathVariable int userId) {
		Iterable<Transactions> getTransactionUser= transactionsRepo.getUserTransactions(userId);
		return getTransactionUser;
	}
	
	@PutMapping("/upload/{transactionsId}")
	public String uploadBuktiTrf(@PathVariable int transactionsId, @RequestParam("file") MultipartFile file) throws JsonMappingException, JsonProcessingException {
		Transactions findTransactions = transactionsRepo.findById(transactionsId).get();
//		findTransactions = new ObjectMapper().readValue(transactionsString, Transactions.class);
		Date date = new Date();
		String fileExtension = file.getContentType().split("/")[1];
		String newFileName = "Bukti_Transfer-" + findTransactions.getUser().getUsername() + date.getTime() + "." + fileExtension;
		String fileName = StringUtils.cleanPath(newFileName);
		Path path = Paths.get(StringUtils.cleanPath(uploadPath) + fileName);
		try {
			Files.copy(file.getInputStream(), path,StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			e.printStackTrace();
		}
		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/transactions/download/").path(fileName).toUriString();
		findTransactions.setBuktiTrf(fileDownloadUri);
		findTransactions.setStatus("pending");
		transactionsRepo.save(findTransactions);
		return fileDownloadUri;
	}
	
	@GetMapping("/download/{fileName:.+}")
	public ResponseEntity<Object> downloadFile(@PathVariable String fileName){
		Path path = Paths.get(uploadPath + fileName);
		Resource resource = null;
		
		try {
			resource = new UrlResource(path.toUri());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		System.out.println("DOWNLOAD");
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/octet-stream")).header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\""+ resource.getFilename()+ "\"").body(resource);
	}
	
	@PutMapping("/reject/{transactionsId}")
	public Transactions rejectTransactions(@PathVariable int transactionsId) {
		Transactions findTransactions = transactionsRepo.findById(transactionsId).get();
		findTransactions.setStatus("Harap Kirim Ulang Bukti Pembayaran");
		findTransactions.setBuktiTrf(null);
		return transactionsRepo.save(findTransactions);
	}
	
	int contoh3 = 9999;
	String message ="";
	int index = 1;
	@PutMapping("/accept/{transactionsId}")
	public Transactions acceptTransactions(@PathVariable int transactionsId) {
		Transactions findTransactions = transactionsRepo.findById(transactionsId).get();
		findTransactions.setStatus("accepted");
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
		
		findTransactions.setTanggalAcc(formatter.format(date));
		findTransactions.setStatusPengiriman("Sudah Dikirim");
		index = 1;
		message ="";
		findTransactions.getTransactionsDetails().forEach(val ->{
			if (val.getPaket() == null) {
				if (val.getProduct().getPaket() != null) {					
					val.getProduct().setStockGudang(val.getProduct().getStock());
					contoh3 = 9999;
					val.getProduct().setSold(val.getProduct().getSold() + val.getQuantity());
					productRepo.save(val.getProduct());
					val.getProduct().getPaket().getProducts().forEach(value ->{
						if (contoh3 > value.getStock()) {
							contoh3 = value.getStock();
						}
					});
					val.getProduct().getPaket().setStockPaket(contoh3);
					val.getProduct().getPaket().setStockPaketGudang(contoh3);
					paketRepo.save(val.getProduct().getPaket());
				}
				else {
					val.getProduct().setStockGudang(val.getProduct().getStock());
					val.getProduct().setSold(val.getProduct().getSold() + val.getQuantity());
					productRepo.save(val.getProduct());
				}
			}
			else {
				val.getPaket().setStockPaketGudang(val.getPaket().getStockPaket());
				val.getPaket().setSoldPaket(val.getPaket().getSoldPaket() + val.getQuantity());
				paketRepo.save(val.getPaket());
				val.getPaket().getProducts().forEach(value ->{
					value.setStockGudang(value.getStockGudang() - val.getQuantity());
				});
				productRepo.saveAll(val.getPaket().getProducts());
			}
		});
		
		message = "<h1>Selamat! Pembelian Anda Berhasil</h1>\n";
		message += "<h3> Akun dengan username " + findTransactions.getUser().getUsername() + " telah bertransaksi seperti berikut : </h3>\n";
		message += "<h4> Tanggal Beli : " + findTransactions.getTanggalBeli() + "</h4> \n";
		message += "<h4> Tanggal Acc : " + findTransactions.getTanggalAcc() + "</h4> \n";
		message += "<h4> Jasa Pengiriman: " + findTransactions.getJasaPengiriman() + "</h4> \n";
		message += "<h4> Price : Rp." + findTransactions.getTotalPrice() + "</h4> \n";
		message += "<h4> Status : " + findTransactions.getStatus() +" & "+ findTransactions.getStatusPengiriman() + "</h4> \n";	
		message += "<h4> Dengan Detail Seperti Berikut : </h4> \n";
		findTransactions.getTransactionsDetails().forEach(val ->{
			if (val.getPaket() == null) {				
				message += "<h5>"+index +". "+ val.getProduct().getProductName() +" dengan harga Rp."+ val.getPrice()+", sebanyak "
			+ val.getQuantity()+ " pcs. Total harga sebanyak Quantity : Rp."+ val.getTotalPriceProduct() + ". (Product)"+ "</h5> \n";
			}
			else {
				message += "<h5>"+index +". "+ val.getPaket().getNamaPaket() +" dengan harga "+ val.getPrice()+" sebanyak "
						+ val.getQuantity()+ " total harga sebanyak Quantity adalah : Rp."+ val.getTotalPriceProduct()+". (Paket)" +"</h5> \n";
			}
			index++;
		});
		String judulPesan = "INVOICE " + findTransactions.getUser().getUsername() + " " + findTransactions.getTanggalAcc();
		emailUtil.sendEmail(findTransactions.getUser().getEmail(), judulPesan, message);
		transactionsRepo.save(findTransactions);
		return findTransactions;
		
	}
}

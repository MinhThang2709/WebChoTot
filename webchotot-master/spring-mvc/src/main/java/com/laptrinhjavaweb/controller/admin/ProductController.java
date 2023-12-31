package com.laptrinhjavaweb.controller.admin;

import java.util.Base64;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.laptrinhjavaweb.dto.CommentDTO;
import com.laptrinhjavaweb.dto.ProductDTO;
import com.laptrinhjavaweb.dto.ProductDetailDTO;
import com.laptrinhjavaweb.dto.ProductImageDTO;
import com.laptrinhjavaweb.dto.UserDTO;
import com.laptrinhjavaweb.service.ICategoryService;
import com.laptrinhjavaweb.service.ICommentService;
import com.laptrinhjavaweb.service.INewService;
import com.laptrinhjavaweb.service.ISlideService;
import com.laptrinhjavaweb.service.IUserService;
import com.laptrinhjavaweb.util.UploadFileUtils;

@Controller(value = "newControllerOfAdmin")
public class ProductController {
    
	@Autowired
	private INewService newService;
	
	@Autowired
	private ICategoryService categoryService;

	@Autowired
	private ICommentService commentService;
	
	@Autowired
	private IUserService userService;
	
	@Autowired
	private ISlideService slideService;
	
	@Autowired
	private UploadFileUtils uploadFileUtils;
	
	@RequestMapping(value = "/admin-new", method = RequestMethod.GET)
	public ModelAndView listPage(@RequestParam("page") int page, 
			                     @RequestParam("limit") int limit) {
		ModelAndView mav = new ModelAndView("admin/new/list");
		ProductDTO models = new ProductDTO();
		models.setLimit(limit);
		models.setPage(page);
		Sort sort = new Sort(Sort.Direction.ASC, "title");
		Pageable pageable = new PageRequest(page-1, limit, sort);
		models.setListResult(newService.findAll(pageable));
	
		models.setTotalItem(newService.getTotalItem());
		models.setTotalPage((int) Math.ceil((double) models.getTotalItem() / models.getLimit()));
		mav.addObject("model",models);
		return mav;
	}
	
	@RequestMapping(value = "/admin-new/detail", method = RequestMethod.GET)
	public ModelAndView detailProductAdminPage(@RequestParam(value = "id",required = false)Long id) {
		ModelAndView mav = new ModelAndView("admin/new/detail");
		ProductDTO productDto = newService.findById(id);
		productDto.setListDetail(newService.findListProductDetailByProduct(id));
		mav.addObject("model",productDto);
		return mav;
	}
	@RequestMapping(value = "/admin-new/detail/create", method = RequestMethod.GET)
	public ModelAndView createdetailProductAdminPage(@RequestParam(value = "id",required = false)Long id) {
		ModelAndView mav = new ModelAndView("admin/new/createDetail");
		return mav;
	}
	@RequestMapping(value = "/admin-new/detail/update", method = RequestMethod.GET)
	public ModelAndView updatedetailProductAdminPage(@RequestParam(value = "id",required = false)Long id) {
		ModelAndView mav = new ModelAndView("admin/new/updateDetail");
		ProductDetailDTO dto = newService.findDetailByID(id);
		mav.addObject("model",dto);
		
		return mav;
	}
	
	
	
	@RequestMapping(value = "/admin-new/create", method = RequestMethod.GET)
	public ModelAndView CreatePage(@RequestParam(value = "id",required = false)Long id) {
		ModelAndView mav = new ModelAndView("admin/new/create");
		ProductDTO model = new ProductDTO();
		if( id != null){
			model = newService.findById(id);
		}
		mav.addObject("categories", categoryService.findAll());
		mav.addObject("model",model);
		return mav;
	}
	
	@RequestMapping(value = "/admin-new/addImg", method = RequestMethod.GET)
	public ModelAndView addImgPage(@RequestParam(value = "id",required = false)Long id) {
		ModelAndView mav = new ModelAndView("admin/new/addimg");	
		return mav;
	}
	@RequestMapping(value = "/product/detail", method = RequestMethod.GET)
	public ModelAndView detailProductPage(@RequestParam(value = "id",required = false)Long id) {
		ModelAndView mav = new ModelAndView("web/product/detail");
		ProductDTO model = new ProductDTO();
		CommentDTO cmtModel = new CommentDTO();
		UserDTO userModel = new UserDTO();
		cmtModel.setListResult(commentService.findAllByProductId(id));
		model = newService.findById(id);
		model.setType("addview");
		newService.save(model);
		model.setListDetail(newService.findListProductDetailByProduct(id));
		try {
			userModel = userService.findByUserNameAndStatus(model.getCreatedBy());
		} catch (Exception e) {
			// TODO: handle exception
		}
		
	//	model.setListImg(newService.findListImgageByProduct(id));
		mav.addObject("model",model);
		mav.addObject("cmt",cmtModel);
		mav.addObject("userModel",userModel);
		return mav;
	}

	@RequestMapping(value = "/api/createProduct", method = RequestMethod.POST)
	public ModelAndView createProduct(@ModelAttribute ProductDTO product,HttpServletRequest request) throws InterruptedException {
		
		 try {
	            byte[] decodeBase64 = Base64.getDecoder().decode(product.getBase64().getBytes());
	            uploadFileUtils.writeOrUpdate(decodeBase64, "/thumbnail/"+product.getThumbnail());
	           
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		 Thread.sleep(4500);
		 product = newService.save(product);
		 
		 if(request.getParameter("type").equals("web")){
			
			 String url = "redirect:/user/product?id="+ request.getParameter("userId");
			 ModelAndView mav = new ModelAndView(url);
			 if(request.getParameter("id") != null){
				 mav.addObject("message","Cập nhật phòng thành công");
			 }else{
				 mav.addObject("message","Tạo mới tin đăng thành công");
			 }
			 return mav;
		 }
		 ModelAndView mav = new ModelAndView("redirect:/admin-new?page=1&limit=5");
		 if(request.getParameter("id") != null){
			 mav.addObject("message","Cập nhật phòng thành công");
		 }else{
			 mav.addObject("message","Tạo mới tin đăng thành công");
		 }
		 return mav;
	}
	
	@RequestMapping(value = "/api/deleteProduct", method = RequestMethod.POST)
	public ModelAndView deleteProduct(HttpServletRequest request) {
		  String []a = request.getParameter("ids").split(",");
		  int size = a.length;
	      long [] arr = new long [size];
	      for(int i=0; i<size; i++) {
	         arr[i] = Long.parseLong(a[i]);
	      }		
	      newService.delete(arr);
	      if(request.getParameter("type").equals("web")){
				 String url = "redirect:/user/product?id="+ request.getParameter("userId");
				 ModelAndView mav = new ModelAndView(url);
				 mav.addObject("message","Xóa tin đăng thành công");
				 return mav;
			 }
	      ModelAndView mav = new ModelAndView("redirect:/admin-new?page=1&limit=5");
	      mav.addObject("message","Xóa tin đăng thành công");
		 return mav;
	}
	//====================== PRODUCT DETAIL ===============================
	@RequestMapping(value = "/api/createDetail", method = RequestMethod.POST)
	public ModelAndView createDetail(@ModelAttribute ProductDetailDTO detail,HttpServletRequest request) throws InterruptedException {
		
		 try {
	            byte[] decodeBase64 = Base64.getDecoder().decode(detail.getBase64().getBytes());
	            uploadFileUtils.writeOrUpdate(decodeBase64, "/thumbnail/"+detail.getThumbnail());
	          
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		 Thread.sleep(4500);
		 detail = newService.saveDetail(detail);
		 
		 if(request.getParameter("type").equals("web")){
			 String url = "redirect:/user/product/detail?id="+ request.getParameter("productId");
			 ModelAndView mav = new ModelAndView(url);
			 if(request.getParameter("id") != null){
				 mav.addObject("message","Cập nhật chi tiết phòng thành công");
			 }else{
				 mav.addObject("message","Tạo mới chi tiết phòng thành công");
			 }
			 return mav;
		 }
		 ModelAndView mav = new ModelAndView("redirect:/admin-new?page=1&limit=5");
		 if(request.getParameter("id") != null){
			 mav.addObject("message","Cập nhật chi tiết phòng thành công");
		 }else{
			 mav.addObject("message","Tạo mới chi tiết phòng thành công");
		 }
		 return mav;
	}
	
	@RequestMapping(value = "/api/deleteDetail", method = RequestMethod.POST)
	public ModelAndView deleteDetail(HttpServletRequest request) {
		  String []a = request.getParameter("ids").split(",");
		  int size = a.length;
	      long [] arr = new long [size];
	      for(int i=0; i<size; i++) {
	         arr[i] = Long.parseLong(a[i]);
	      }		
	      newService.deleteDetail(arr);
	      if(request.getParameter("type").equals("web")){
				 String url = "redirect:/user/product/detail?id="+ request.getParameter("productId");
				 ModelAndView mav = new ModelAndView(url);
				 mav.addObject("message","Xóa chi tiết phòng thành công");
				 return mav;
			 }
	      ModelAndView mav = new ModelAndView("redirect:/admin-new?page=1&limit=5");
	      mav.addObject("message","Xóa chi tiết phòng thành công");
		 return mav;
	}
	//====================== IMAGE ====================================
	@RequestMapping(value = "/api/createImg", method = RequestMethod.POST)
	public ModelAndView createImg(@ModelAttribute ProductImageDTO img,HttpServletRequest request) throws InterruptedException {
		 
		 try {
	            byte[] decodeBase64 = Base64.getDecoder().decode(img.getBase64().getBytes());
	            uploadFileUtils.writeOrUpdate(decodeBase64, "/thumbnail/"+img.getThumbnail());
	    
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		 Thread.sleep(4500);
		 img = newService.saveImg(img);
		 if(request.getParameter("type").equals("web")){
			 String url = "redirect:/user/product/detail?id="+ request.getParameter("pdId");
			 ModelAndView mav = new ModelAndView(url);
			 if(request.getParameter("id") != null){
				 mav.addObject("message","Cập nhật hình ảnh phòng thành công");
			 }else{
				 mav.addObject("message","Tạo mới hình ảnh phòng thành công");
			 }
			 return mav;
		 }
		 ModelAndView mav = new ModelAndView("redirect:/admin-new?page=1&limit=5");
		 if(request.getParameter("id") != null){
			 mav.addObject("message","Cập nhật hình ảnh phòng thành công");
		 }else{
			 mav.addObject("message","Tạo mới hình ảnh phòng thành công");
		 }
		 return mav;
	}
	@RequestMapping(value = "/api/deleteImg", method = RequestMethod.POST)
	public ModelAndView deleteImg(HttpServletRequest request) {
		  String []a = request.getParameter("ids").split(",");
		  int size = a.length;
	      long [] arr = new long [size];
	      for(int i=0; i<size; i++) {
	         arr[i] = Long.parseLong(a[i]);
	      }		
	      newService.deleteImg(arr);
	      if(request.getParameter("type").equals("web")){
				 String url = "redirect:/user/product/detail?id="+ request.getParameter("productId");
				 ModelAndView mav = new ModelAndView(url);
				 mav.addObject("message","Xóa hình ảnh phòng thành công");
				 return mav;
			 }
	      ModelAndView mav = new ModelAndView("redirect:/admin-new?page=1&limit=5");
	      mav.addObject("message","Xóa hình ảnh phòng thành công");
		 return mav;
	}
	
}

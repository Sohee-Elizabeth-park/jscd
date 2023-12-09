package com.jscd.app.board.qna.qnaController;

import com.jscd.app.board.qna.qnaDao.AllqnaDao;
import com.jscd.app.board.qna.qnaDto.AllqnaDto;
import com.jscd.app.board.qna.qnaDto.AllqnacDto;
import com.jscd.app.board.qna.qnaDto.PageHandler;
import com.jscd.app.board.qna.qnaDto.SearchCondition;
import com.jscd.app.board.qna.qnaService.AllqnaCmmtService;
import com.jscd.app.board.qna.qnaService.AllqnaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Controller
@RequestMapping("/board/qna/*")
public class AllqnaController {

    private static final Logger logger = Logger.getLogger(AllqnaController.class.getName());
    @Autowired
    AllqnaService allqnaService;
    @Autowired
    AllqnaDao allqnaDao;


    //1-1. 게시글 등록
    @GetMapping("/allqnaWrite")
    public String write(Model model) {
        model.addAttribute("mode", "new");
        return "/board/qna/allqna";
    }

    @PostMapping("/allqnaWrite")
    public String write(Model model, AllqnaDto allqnaDto, HttpSession session, RedirectAttributes rttr) {

        String writer = (String) session.getAttribute("id");
        allqnaDto.setWriter(writer);

        try {
            if (allqnaService.write(allqnaDto) != 1) {
                throw new Exception("Write faild");
            }
            rttr.addFlashAttribute("msg", "WRITE_OK");
            return "redirect:/board/qna/allqnaList";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute(allqnaDto);
            model.addAttribute("mode", "new");
            model.addAttribute("msg", "WRITE_ERR");
            return "/board/qna/allqnaList";
        }


    }

    //1-2. 게시글 목록 읽기 (페이징 처리) 페이지 이동
    @GetMapping("/allqnaList")
    public String allqnaList(SearchCondition sc, Model model, AllqnaDto allqnaDto) throws Exception {

        try {

            int totalCnt = allqnaService.getSearchResultCnt(sc);
            model.addAttribute("totalCnt", totalCnt);

            PageHandler pageHandler = new PageHandler(totalCnt, sc);

            List<AllqnaDto> list = allqnaService.getSearchResultPage(sc);
            model.addAttribute("list", list);
            model.addAttribute("ph", pageHandler);

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("msg", "LIST_ERR");
            model.addAttribute("totalCnt", 0);
        }
        return "/board/qna/allqnaList";
    }

    //1-3. 게시글 상세페이지 및 수정
    @GetMapping("/allqnaDetail")
    public String read(Integer allqnaNo, SearchCondition sc, RedirectAttributes rttr, Model model) {
        try {
            //상세 내용
            AllqnaDto allqnaDto = allqnaService.read(allqnaNo);
            model.addAttribute("read", allqnaDto);

            //댓글
            List<AllqnacDto> list = allqnaService.cmmtRead(allqnaNo);
            model.addAttribute("comment", list);
        } catch (Exception e) {
            e.printStackTrace();
            rttr.addFlashAttribute("msg", "READ_ERR");
            return "redirect:/board/qna/allqna" + sc.getQueryString();
        }


     return "board/qna/allqnaCont";
    }

    //게시글 수정하기 버튼 눌렀을때
    @GetMapping("allqnaModify")
    public String allqnaModify(@RequestParam("allqnaNo") Integer allqnaNo, Model model) throws Exception {
        AllqnaDto allqnaDto = allqnaService.read(allqnaNo);
        model.addAttribute("modify", allqnaDto);
        return "/board/qna/allqnaModify";
    }


    //수정된 내용 등록하기
    @PostMapping("/allqnaModified")
    public String allqnaModified(AllqnaDto allqnaDto, Model model, RedirectAttributes rttr) throws Exception {

        allqnaService.modify(allqnaDto);
        model.addAttribute("modified", allqnaDto);

        rttr.addFlashAttribute("result", "modify success");

        return "redirect:/board/qna/allqnaList";

    }


    //1-4. 게시글 삭제
    @GetMapping("/allqnaDelete")
    public String remove(Integer allqnaNo, SearchCondition sc, RedirectAttributes rattr, HttpSession session) {
        String writer = (String) session.getAttribute("id");
        String msg = "DEL_OK";

        try {
            if (allqnaService.remove(allqnaNo, writer) != 1)
                throw new Exception("Delete failed.");
        } catch (Exception e) {
            e.printStackTrace();
            msg = "DEL_ERR";
        }

        rattr.addFlashAttribute("msg", msg);
        return "redirect:/board/qna/allqnaList" + sc.getQueryString();
    }

    //2-1 댓글 등록
    @PostMapping("/cmmtWrite")
    public String write(AllqnacDto allqnacDto, Integer allqnaNo, HttpSession session) throws Exception {

        allqnacDto.setAllqnaNo(allqnaNo);
        allqnaService.cmmtWrite(allqnacDto);
        return "redirect:/board/qna/allqnaDetail?allqnaNo=" + allqnacDto.getAllqnaNo();

    }


    //2-2 댓글 목록 (게시글 리스트에 있음)

//
//    2-3 댓글 수정
    @PostMapping("/cmmtModify")
    public String cmmtModify(@RequestBody AllqnacDto allqnacDto) throws Exception {
        System.out.println("컨트롤러 댓글수정 : " + allqnacDto);
        allqnaDao.cmmtUpdate(allqnacDto);


        return "/board/qna/allqnaCont";

    }
//    @PostMapping("/cmmtModify")
//    public HashMap<String, Object> cmmtModify(@RequestBody AllqnacDto allqnacDto) {
//        // 여기서 yourData 객체는 전송된 JSON 데이터를 자동으로 매핑한 객체입니다.
//        // yourData를 처리하고 결과를 HashMap<String, Object>로 반환합니다.
//        System.out.println("컨트롤러 댓글수정 : " + allqnacDto);
//        System.out.println("컨트롤러 : "+allqnacDto.getAllqnaCNo());
//        System.out.println("컨트롤러 : "+allqnacDto.getContent());
//      System.out.println("컨트롤러 : "+allqnacDto.getWriter());
//        HashMap<String, Object> response = new HashMap<>();
//        // 처리 로직
//        // response.put("key", value); // 필요한 응답 데이터 추가
//        response.put("allqnaCno", allqnacDto.getAllqnaCNo());
//
//
//        return response;
//    }



    //2-4 댓글 삭제
    @PostMapping("/cmmtRemove")
    public String cmmtRemove(@RequestBody AllqnacDto allqnacDto) throws Exception {
        Integer allqnaCNo = allqnacDto.getAllqnaCNo();
        System.out.println("컨트롤러: " + allqnaCNo);
        allqnaService.cmmtRemove(allqnaCNo);

        return "redirect:/board/qna/allqnaDetail?allqnaNo=" + allqnacDto.getAllqnaNo();
        }


//3-1 대댓글 등록
//3-2 대댓글 목록
//3-3 대댓글 수정
//3-4 대댓글 삭제

//4 비밀글 제외
//5 내가 작성한 글 보기




}











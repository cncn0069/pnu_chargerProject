package charger.main.util;

import java.util.Map;

import org.springframework.security.oauth2.core.user.OAuth2User;

public class CustomMyUtil {
	@SuppressWarnings("unchecked")
	public static String getUsernameFromOAuth2User(OAuth2User user) {
		Map<String, Object> attempt = user.getAttributes();
		String userString = (String)user.toString();
		String ret = "";
		

		if(userString.contains("https://www.googleapis.com")) {
			ret = "Google_" + attempt.get("name") + "_" + attempt.get("sub");
		}else if(userString.contains("response=")) {
			Map<String,Object> resmap = (Map<String, Object>)attempt.get("response");
			ret = "Naver_" + resmap.get("name") + "-" + resmap.get("id");
		}else if (userString.contains("https://k.kakaocdn.net")) {
			Map<String, String> propmap = (Map<String, String>)attempt.get("properties");
			ret = "Kakao_" + propmap.get("nickname") + "_" + attempt.get("id");
		}else if(userString.contains("https://api.github.com")) {
			ret = "Github_" + attempt.get("login") + "_" + attempt.get("id"); 
		}else {
			ret =  "Facebook_" + attempt.get("name") + "_" + attempt.get("id");
		}
		
		ret = ret.replaceAll(",", "_").replaceAll(" ", "_");
		return ret;
	}

}

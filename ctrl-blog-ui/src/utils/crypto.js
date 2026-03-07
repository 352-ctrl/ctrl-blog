/**
 * =========================================================================
 * 声明：
 * 本文件属于 AJ-Captcha 行为验证码组件依赖。
 * 原项目主页：https://gitee.com/belief-team/captcha
 * =========================================================================
 */
import CryptoJS from 'crypto-js'
/**
 * @word 要加密的内容
 * @keyWord String  服务器随机返回的关键字
 *  */
export function aesEncrypt(word,keyWord="XwKsGlMcdPMEhR1B"){
  var key = CryptoJS.enc.Utf8.parse(keyWord);
  var srcs = CryptoJS.enc.Utf8.parse(word);
  var encrypted = CryptoJS.AES.encrypt(srcs, key, {mode:CryptoJS.mode.ECB,padding: CryptoJS.pad.Pkcs7});
  return encrypted.toString();
}

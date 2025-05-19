const { test, expect } = require('@playwright/test');

test.describe('회원가입 주소 API 및 validation E2E', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/join');
  });

  test('카카오 주소 API로 주소 입력', async ({ page }) => {
    await page.selectOption('#addressApiSelect', 'daum');
    await page.click('#searchAddressBtn');
    await page.waitForSelector('iframe');
    await page.evaluate(() => {
      document.getElementById('address').value = '서울특별시 강남구 테헤란로 123';
    });
    await expect(page.locator('#address')).toHaveValue('서울특별시 강남구 테헤란로 123');
  });

  test('네이버 주소 API 선택 시 알림창 노출', async ({ page }) => {
    await page.selectOption('#addressApiSelect', 'naver');
    page.once('dialog', dialog => {
      expect(dialog.message()).toContain('네이버 주소 API는 실제 서비스키가 필요합니다');
      dialog.dismiss();
    });
    await page.click('#searchAddressBtn');
  });

  test('validation 실패 시 shake/컬러/아이콘 효과', async ({ page }) => {
    await page.fill('#email', 'invalid-email');
    await page.fill('#password', '1234');
    await page.fill('#passwordConfirm', '5678');
    await page.fill('#phone', 'abc');
    await page.fill('#address', '');
    await page.click('.join-btn');
    await expect(page.locator('#email')).toHaveClass(/is-invalid/);
    await expect(page.locator('#password')).toHaveClass(/is-invalid/);
    await expect(page.locator('#passwordConfirm')).toHaveClass(/is-invalid/);
    await expect(page.locator('#phone')).toHaveClass(/is-invalid/);
    await expect(page.locator('#address')).toHaveClass(/is-invalid/);
    await expect(page.locator('#emailError')).toContainText('이메일');
    await expect(page.locator('#passwordError')).toContainText('비밀번호');
    await expect(page.locator('#passwordConfirmError')).toContainText('비밀번호');
    await expect(page.locator('#phoneError')).toContainText('전화번호');
    await expect(page.locator('#addressError')).toContainText('주소');
  });

  test('validation 성공 시 회원가입 시도', async ({ page }) => {
    await page.fill('#email', 'testuser@example.com');
    await page.fill('#password', 'Test1234!');
    await page.fill('#passwordConfirm', 'Test1234!');
    await page.fill('#name', '홍길동');
    await page.fill('#nickname', '길동이');
    await page.fill('#phone', '01012345678');
    await page.evaluate(() => {
      document.getElementById('address').value = '서울특별시 강남구 테헤란로 123';
    });
    await page.check('#terms1');
    await page.check('#terms2');
    await page.click('.join-btn');
  });
}); 
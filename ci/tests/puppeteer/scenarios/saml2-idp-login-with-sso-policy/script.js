const puppeteer = require("puppeteer");
const path = require("path");
const cas = require("../../cas.js");

(async () => {
    const browser = await puppeteer.launch(cas.browserOptions());
    const page = await cas.newPage(browser);

    await cas.gotoLogin(page);
    await page.waitForTimeout(1000);
    await cas.loginWith(page);
    
    await cas.goto(page, "http://localhost:9443/simplesaml/module.php/core/authenticate.php?as=default-sp");
    await page.waitForTimeout(2000);
    await cas.removeDirectoryOrFile(path.join(__dirname, "/saml-md"));
    await cas.assertVisibility(page, "#username");
    await cas.assertVisibility(page, "#password");

    await browser.close();
})();



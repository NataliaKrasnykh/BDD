package ru.netology.javaqa.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.javaqa.data.DataHelper;
import ru.netology.javaqa.page.DashboardPage;
import ru.netology.javaqa.page.LoginPage;

import static com.codeborne.selenide.Selenide.open;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.netology.javaqa.data.DataHelper.*;

public class MoneyTransferTest {
    DashboardPage dashboardPage;
    CardInfo firstCardInfo;
    CardInfo secondCardInfo;
    int firstCardBalance;
    int secondCardBalance;

    @BeforeEach
    void setup() {
        var loginPage = open("http://localhost:9999", LoginPage.class);
        var authInfo = DataHelper.getAuthInfo();
        var verificationPage = loginPage.validLogin(authInfo);
        var verificationCode = DataHelper.getVerificationCode();
        dashboardPage = verificationPage.validVerify(verificationCode);
        firstCardInfo = DataHelper.getFirstCardInfo();
        secondCardInfo = DataHelper.getSecondCardInfo();
        firstCardBalance = dashboardPage.getCardBalance(firstCardInfo);
        secondCardBalance = dashboardPage.getCardBalance(secondCardInfo);
    }

    @Test
    void shouldTransferMoneyBetweenOwnCards() {
        var amount = generateValidAmount(firstCardBalance);
        var expectedDBalanceFirstCard = firstCardBalance - amount;
        var expectedDBalanceSecondCard = secondCardBalance + amount;
        var transferPage = dashboardPage.selectCardToTransfer(secondCardInfo);
        dashboardPage = transferPage.makeValidTransfer(String.valueOf(amount), firstCardInfo);
        dashboardPage.reloadDashboardPage();
        var actualBalanceFirstCard = dashboardPage.getCardBalance(firstCardInfo);
        var actualBalanceSecondCard = dashboardPage.getCardBalance(secondCardInfo);
        assertAll(() -> assertEquals(expectedDBalanceFirstCard, actualBalanceFirstCard),
                () -> assertEquals(expectedDBalanceSecondCard, actualBalanceSecondCard));
    }

    @Test
    void shouldGetErrorMessageIfAmountMoreBalance() {
        var amount = generateInValidAmount(secondCardBalance);
        var transferPage = dashboardPage.selectCardToTransfer(firstCardInfo);
        transferPage.makeTransfer((String.valueOf(amount)), secondCardInfo);
        assertAll(() -> transferPage.findErrorMessage("Выполнена попытка перевода суммы, превышающей остаток на карте списания"),
                () -> dashboardPage.reloadDashboardPage(),
                () -> assertEquals(firstCardBalance, dashboardPage.getCardBalance(firstCardInfo)),
                () -> assertEquals(secondCardBalance, dashboardPage.getCardBalance(secondCardInfo)));
    }
}
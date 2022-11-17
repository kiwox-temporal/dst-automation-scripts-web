package net.kiwox.dst.script;

import net.kiwox.dst.script.appium.TestUtils.*;
import net.kiwox.dst.script.chrome.testflows.EntelWebAuthenticationTest;
import net.kiwox.dst.script.command.*;
import net.kiwox.dst.script.command.chrome.EntelWebBalanceCommand;
import net.kiwox.dst.script.command.chrome.testflows.EntelWebAuthenticationCommand;
import net.kiwox.dst.script.command.chrome.testflows.EntelWebVerfiyHistoricReceiptsPostPaidCommand;
import net.kiwox.dst.script.command.chrome.testflows.EntelWebVerfiyPostPaidBagCommand;
import net.kiwox.dst.script.command.chrome.testflows.EntelWebVerfiyPostPaidBalanceCommand;

import static net.kiwox.dst.script.appium.TestUtils.*;

public class DstMain {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("No arguments found"); // NOSONAR
            return;
        }

        ICommand command;
        switch (args[0]) {
            case "apn":
                command = new ApnCommand();
                break;
            case "upload":
                command = new UploadCommand();
                break;
            case "download":
                command = new DownloadCommand();
                break;
            case "speedtest":
                command = new SpeedTestCommand();
                break;
            case "facebook":
                command = new FacebookCommand();
                break;
            case "instagram":
                command = new InstagramCommand();
                break;
            case "whatsappText":
                command = new WhatsappTextCommand();
                break;
            case "whatsappPhoto":
                command = new WhatsappPhotoCommand();
                break;
            case "twitter":
                command = new TwitterCommand();
                break;
            case "web":
                command = new WebCommand();
                break;
            case "youtube":
                command = new YoutubeCommand();
                break;
            case "tiktok":
                command = new TikTokCommand();
                break;
            // Mobile Automation Tests
            case "app-entel-peru-inicio-sesion": // Base Requirement
                command = new EntelPeruAppCommandSignIn();
                break;
            case "app-entel-peru-verificar-saldo-post-pago": // R01
                command = new EntelPeruAppCommandVerifyPostPaidBalance();
                break;
            case "app-entel-peru-verificar-bolsa-post-pago": // R02
                command = new EntelPeruAppCommandVerifyPostPaidBag();
                break;
            case "app-entel-peru-verificar-recibo": // R03
                command = new EntelPeruAppCommandVerifyReceipt();
                break;
            case "app-entel-peru-verificar-bolsa-pre-pago": // R04
                command = new EntelPeruAppCommandVerifyPrePaidBag();
                break;
            case "app-entel-peru-verificar-recarga-pre-pago": // R05
                command = new EntelPeruAppCommandVerifyPrePaidRecharge();
                break;
            case "app-entel-peru-verificar-beneficios-post-pago": // R06
                command = new EntelPeruAppCommandVerifyPostPaidBenefit();
                break;
            case "app-entel-peru-verificar-linea-adicional-post-pago": // R07
                command = new EntelPeruAppCommandVerifyPostPaidAdditionaLine();
                break;
            case "app-entel-peru-verificar-upselling": // R08
                command = new EntelPeruAppCommandVerifyUpselling();
                break;
            case "app-entel-peru-verificar-renovacion": // R09
                command = new EntelPeruAppCommandVerifyRenewal();
                break;
            case "app-entel-peru-verificar-afiliacion-recibo-post-pago": // R10
                command = new EntelPeruAppCommandVerifyPostPaidMembershipReceipt();
                break;

            // Web App Autoamtion Tests

            case CLI_EXECUTE_AUTHENTICATION_00_ENTEL_WEB_APP:
                command = new EntelWebAuthenticationCommand();
                break;
            case CLI_EXECUTE_VERIFY_POST_PAID_BALANCE_01_ENTEL_WEB_APP:
                command = new EntelWebVerfiyPostPaidBalanceCommand();
                break;
            case CLI_EXECUTE_VERIFY_POST_PAID_BAG_02_ENTEL_WEB_APP:
                command = new EntelWebVerfiyPostPaidBagCommand();
                break;

            case CLI_EXECUTE_VERIFY_HISTORIC_RECEIPTS_POST_PAID_04_ENTEL_WEB_APP:
                command = new EntelWebVerfiyHistoricReceiptsPostPaidCommand();
                break;
            default:
                System.out.println("No command defined for argument [" + args[0] + "]"); // NOSONAR
                return;
        }

        ;

        String[] commandArgs = new String[args.length - 1];
        for (int i = 1; i < args.length; ++i) {
            commandArgs[i - 1] = args[i];
        }
        command.runTest(commandArgs);
    }

}

<#import "template.ftl" as layout>
<@layout.registrationLayout displayInfo=true; section>
    <#if section = "title">
        Verfiy phone number
    <#elseif section = "header">
        Verify phone number
    <#elseif section = "form">
<ol id="kc-totp-settings">
    <form action="${url.loginAction}" class="${properties.kcFormClass!}" id="kc-verify-phone-form" method="post">
        <div class="${properties.kcFormGroupClass!}">
            <div class="${properties.kcInputWrapperClass!}">
                <input type="text" id="code" name="code" autocomplete="off" class="${properties.kcInputClass!}" />
            </div>
        </div>

        <input class="${properties.kcButtonClass!} ${properties.kcButtonPrimaryClass!} ${properties.kcButtonLargeClass!}" type="submit" value="${msg("doSubmit")}"/>
    </form>
    </#if>
</@layout.registrationLayout>

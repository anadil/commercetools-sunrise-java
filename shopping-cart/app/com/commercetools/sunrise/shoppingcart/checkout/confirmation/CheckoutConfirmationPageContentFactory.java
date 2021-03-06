package com.commercetools.sunrise.shoppingcart.checkout.confirmation;

import com.commercetools.sunrise.common.contexts.UserContext;
import com.commercetools.sunrise.common.template.i18n.I18nIdentifier;
import com.commercetools.sunrise.common.template.i18n.I18nIdentifierFactory;
import com.commercetools.sunrise.common.template.i18n.I18nResolver;
import com.commercetools.sunrise.shoppingcart.CartBeanFactory;
import io.sphere.sdk.carts.Cart;
import io.sphere.sdk.models.Base;
import play.data.Form;

import javax.inject.Inject;

public class CheckoutConfirmationPageContentFactory extends Base {

    @Inject
    private CartBeanFactory cartLikeBeanFactory;
    @Inject
    private UserContext userContext;
    @Inject
    private I18nResolver i18nResolver;
    @Inject
    private I18nIdentifierFactory i18nIdentifierFactory;

    public CheckoutConfirmationPageContent create(final Form<?> form, final Cart cart) {
        final CheckoutConfirmationPageContent bean = new CheckoutConfirmationPageContent();
        initialize(bean, form, cart);
        return bean;
    }

    protected final void initialize(final CheckoutConfirmationPageContent bean, final Form<?> form, final Cart cart) {
        fillCart(bean, cart);
        fillTitle(bean, cart);
        fillForm(bean, form);
    }

    protected void fillForm(final CheckoutConfirmationPageContent bean, final Form<?> form) {
        bean.setCheckoutForm(form);
    }

    protected void fillTitle(final CheckoutConfirmationPageContent bean, final Cart cart) {
        final I18nIdentifier i18nIdentifier = i18nIdentifierFactory.create("checkout:confirmationPage.title");
        bean.setTitle(i18nResolver.getOrEmpty(userContext.locales(), i18nIdentifier));
    }

    protected void fillCart(final CheckoutConfirmationPageContent bean, final Cart cart) {
        bean.setCart(cartLikeBeanFactory.create(cart));
    }
}

package hamaster.gradesign.jsf.converter;

import static hamaster.gradesign.IBECSR.APPLICATION_APPROVED;
import static hamaster.gradesign.IBECSR.APPLICATION_DENIED;
import static hamaster.gradesign.IBECSR.APPLICATION_PROCESSING;
import static hamaster.gradesign.IBECSR.APPLICATION_STARTED;
import static hamaster.gradesign.IBECSR.APPLICATION_ERROR;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

//@FacesConverter("requestStatusConverter")
public class RequestStatusConverter implements Converter {

	public RequestStatusConverter() {
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.convert.Converter#getAsObject(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.String)
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if ("申请通过".equals(value))
			return new Integer(APPLICATION_APPROVED);
		if ("申请未通过".equals(value))
			return new Integer(APPLICATION_DENIED);
		if ("处理申请中".equals(value))
			return new Integer(APPLICATION_PROCESSING);
		if ("开始申请".equals(value))
			return new Integer(APPLICATION_STARTED);
		if ("发生错误".equals(value))
			return new Integer(APPLICATION_ERROR);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see javax.faces.convert.Converter#getAsString(javax.faces.context.FacesContext, javax.faces.component.UIComponent, java.lang.Object)
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if (value instanceof Number) {
			int i = ((Number) value).intValue();
			switch (i) {
			case APPLICATION_APPROVED:
				return "申请通过";
			case APPLICATION_STARTED:
				return "开始申请";
			case APPLICATION_DENIED:
				return "申请未通过";
			case APPLICATION_PROCESSING:
				return "处理申请中";
			case APPLICATION_ERROR:
				return "发生错误";
			}
		}
		return new String();
	}
}

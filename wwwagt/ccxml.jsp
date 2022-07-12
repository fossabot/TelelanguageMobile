<?xml version="1.0" encoding="UTF-8"?>

<ccxml version="1.0" xmlns="http://www.w3.org/2002/09/ccxml" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.w3.org/2002/09/ccxml http://www.w3.org/TR/ccxml/ccxml.xsd" xmlns:voxeo="http://community.voxeo.com/xmlns/ccxml">
    <var name="targetAddr" expr="'ccxml/event'"/>
    <var name="sessionid" expr="session.id"/>
    <var name="activeconnections" expr="0"/>
    <var name="filename"/>
    
    <eventprocessor>

        <transition event="SessionDestroy">
			<exit/>
        </transition>

        <transition event="ccxml.kill*">
            <exit/>
        </transition>
        
        <transition event="ConferenceCreate">
            <var name="conferenceid"/>
            <var name="confname" expr="event$.confname"/>
            <createconference conferenceid="conferenceid" confname="confname"/>
        </transition>

        <transition event="conference.created">
			<var name="eventname" expr="'ConferenceCreated'"/>
            <var name="conferenceid" expr="event$.conferenceid"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname conferenceid connectionid"/>
        </transition>

        <transition event="ConferenceDestroy">
            <destroyconference conferenceid="event$.conferenceid"/>
        </transition>

        <transition event="conference.destroyed">
			<var name="eventname" expr="'ConferenceDestroyed'"/>
			<var name="conferenceid" expr="event$.conferenceid"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname"/>
        </transition>
        
        <transition event="ConferenceJoin">
            <if cond="typeof(event$.termdigits) != 'undefined'">
                <join id1="event$.id1" id2="event$.id2" voxeo-termdigits="event$.termdigits" duplex="event$.duplex" entertone="event$.entertone" exittone="'false'"/>
            <else/>
                <join id1="event$.id1" id2="event$.id2" duplex="event$.duplex" entertone="event$.entertone" exittone="'false'"/>
            </if>
        </transition>

        <transition event="conference.joined">
			<var name="eventname" expr="'ConferenceJoined'"/>
            <var name="id1" expr="event$.id1"/>
            <var name="id2" expr="event$.id2"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname id1 id2"/>
        </transition>
        
        <transition event="ConferenceUnjoin">
            <unjoin id1="event$.id1" id2="event$.id2"/>
        </transition>

        <transition event="conference.unjoined">
			<var name="eventname" expr="'ConferenceUnjoined'"/>
            <var name="id1" expr="event$.id1"/>
            <var name="id2" expr="event$.id2"/>
            <var name="termdigit" expr="event$.termdigit"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname id1 id2 termdigit"/>
        </transition>

        <transition event="connection.alerting">
            <var name="eventname" expr="'CallIncoming'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <var name="connectionFrom" expr="event$.connection.protocol.sip.headers['From']"/>
            <var name="connectionTo" expr="event$.connection.protocol.sip.headers['To']"/>
            <var name="sipCallId" expr="event$.connection.protocol.sip.headers['Call-ID']" />
            <assign name="activeconnections" expr="activeconnections + 1"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid connectionLocal connectionRemote connectionFrom connectionTo activeconnections sipCallId"/>
        </transition>
        
        <transition event="CreateCall">
            <assign name="activeconnections" expr="activeconnections + 1"/>
            <if cond="typeof(event$.callerid) != 'undefined'">
                <createcall dest="event$.dest" callerid="event$.callerid" timeout="event$.timeout" />
            <else/>
                <createcall dest="event$.dest" timeout="event$.timeout" />
            </if>
        </transition>
        
        <transition event="connection.progressing">
            <var name="eventname" expr="'CallOutgoing'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <var name="sipCallId" expr="event$.connection.protocol.sip.headers['Call-ID']" />
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid connectionLocal connectionRemote sipCallId"/>
        </transition>
        
        <transition event="CallAnswer">
            <accept connectionid="event$.connectionid"/>
        </transition>

        <transition event="connection.connected">
        	<var name="eventname" expr="'CallConnected'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid connectionLocal connectionRemote"/>
        </transition>
        
        <transition event="RecordCall">
            <assign name="filename" expr="session.connections[event$.connectionid]._RecordCall(100,'IVR-'+event$.recordingtag+';version=2')"/>
        </transition>
        
        <transition event="RecordCallStop">
            <assign name="filename" expr="session.connections[event$.connectionid]._RecordCall(0,'IVR-'+event$.recordingtag+';version=2')"/>
        </transition>
        
        <transition event="connection.accept.failed">
            <var name="eventname" expr="'CallConnectedError'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason"/>
        </transition>
        
        <transition event="CallDisconnect">
            <disconnect connectionid="event$.connectionid" reason="event$.reason"/>
        </transition>

        <transition event="CallRedirect">
            <redirect dest="event$.dest" connectionid="event$.connectionid" reason="event$.reason"/>
        </transition>

        <transition event="connection.disconnected">
			<var name="eventname" expr="'CallDisconnected'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid connectionLocal connectionRemote"/>
            <assign name="activeconnections" expr="activeconnections - 1"/>
            <if cond="activeconnections == 0">
                <assign name="eventname" expr="'SessionDestroyed'"/>
                <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname"/>
            </if>
        </transition>

        <transition event="connection.failed">
            <var name="eventname" expr="'CallConnectionFailed'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason connectionLocal connectionRemote"/>
            <assign name="activeconnections" expr="activeconnections - 1"/>
            <if cond="activeconnections == 0">
                <assign name="eventname" expr="'SessionDestroyed'"/>
                <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname"/>
            </if>
        </transition>
        
        <transition event="CallMerge">
            <merge connectionid1="event$.connectionid1" connectionid2="event$.connectionid2"/>
        </transition>

        <transition event="connection.merged">
            <var name="eventname" expr="'CallConnectionMerged'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <var name="mergeid" expr="event$.mergeid"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid connectionLocal connectionRemote mergeid"/>
            <assign name="activeconnections" expr="activeconnections - 1"/>
            <log expr="'after ' + event + ' activeconnections is ' + activeconnections"/>
            <if cond="activeconnections == 0">
                <assign name="eventname" expr="'SessionDestroyed'"/>
                <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname"/>
            </if>
        </transition>

        <transition event="connection.redirect.failed">
            <var name="eventname" expr="'CallConnectionRedirectFailed'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason"/>
        </transition>

        <transition event="connection.redirected">
            <var name="eventname" expr="'CallConnectionRedirected'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason connectionLocal connectionRemote"/>
        </transition>
        
        <transition event="CallReject">
            <reject connectionid="event$.connectionid" reason="event$.reason"/>
        </transition>

        <transition event="connection.rejected">
            <var name="eventname" expr="'CallConnectionRejected'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason connectionLocal connectionRemote"/>
            <assign name="activeconnections" expr="activeconnections - 1"/>
            <log expr="'after ' + event + ' activeconnections is ' + activeconnections"/>
            <if cond="activeconnections == 0">
                <assign name="eventname" expr="'SessionDestroyed'"/>
                <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname"/>
            </if>
        </transition>

        <transition event="connection.reject.failed">
            <var name="eventname" expr="'CallConnectionRejectFailed'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason"/>
        </transition>

        <transition event="connection.signal">
            <var name="eventname" expr="'ConnectionSignal'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid connectionLocal connectionRemote"/>
        </transition>
        
        <transition event="DialogPrepare">
            <var name="dialogid"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <dialogprepare src="event$.src" connectionid="event$.connectionid" dialogid="dialogid"/>
            <elseif cond="typeof(event$.conferenceid) != 'undefined'"/>
                <dialogprepare src="event$.src" conferenceid="event$.conferenceid" dialogid="dialogid"/>
            <else/>
                <dialogprepare src="event$.src" dialogid="dialogid"/>
            </if>
        </transition>
        
        <transition event="DialogStart">
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <dialogstart src="event$.src" connectionid="event$.connectionid"/>
            <elseif cond="typeof(event$.conferenceid) != 'undefined'"/>
                <dialogstart src="event$.src" conferenceid="event$.conferenceid"/>
            <else/>
                <dialogstart src="event$.src"/>
            </if>
        </transition>
        
        <transition event="dialog.started">
			<var name="eventname" expr="'DialogStarted'"/>
            <var name="connectionid" expr="''"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid conferenceid dialogid"/>
        </transition>
        
        <transition event="DialogTerminate">
            <dialogterminate dialogid="event$.dialogid"/>
        </transition>
        
        <transition event="dialog.prepared">
			<var name="eventname" expr="'DialogPrepared'"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <var name="src" expr="event$.dialog.src"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid conferenceid dialogid src"/>
        </transition>

        <transition event="dialog.disconnect">
            <var name="eventname" expr="'DialogDisconnect'"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="connectionid" expr="''"/>
            <var name="dialogreason" expr="'disconnected'"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname dialogid connectionid conferenceid dialogreason"/>
        </transition>

        <transition event="dialog.exit">
			<var name="eventname" expr="'DialogExit'"/>
            <var name="connectionid" expr="''"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="conferenceid" expr="''"/>
            <var name="validated" expr="''"/>
            <var name="accesscode" expr="''"/>
            <var name="language" expr="''"/>
            <var name="deptcode" expr="''"/>
            <var name="dtmfpin" expr="''"/>
            <var name="calltype" expr="''"/>
            <var name="action" expr="''"/>
            <var name="phonenumber" expr="''"/>
            <var name="dialogreason" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <if cond="typeof(event$.values.accesscode) != 'undefined'">
                <assign name="accesscode" expr="event$.values.accesscode"/>
            </if>
            <if cond="typeof(event$.values.language) != 'undefined'">
                <assign name="language" expr="event$.values.language"/>
            </if>
            <if cond="typeof(event$.values.deptcode) != 'undefined'">
                <assign name="deptcode" expr="event$.values.deptcode"/>
            </if>
            <if cond="typeof(event$.values.dtmfpin) != 'undefined'">
                <assign name="dtmfpin" expr="event$.values.dtmfpin"/>
            </if>
            <if cond="typeof(event$.values.calltype) != 'undefined'">
                <assign name="calltype" expr="event$.values.calltype"/>
            </if>
            <if cond="typeof(event$.values.validated) != 'undefined'">
                <assign name="validated" expr="event$.values.validated"/>
            </if>
            <if cond="typeof(event$.values.dialogreason) != 'undefined'">
                <assign name="dialogreason" expr="event$.values.dialogreason"/>
            </if>
            <if cond="typeof(event$.values.validated) != 'undefined'">
                <assign name="action" expr="event$.values.action"/>
            </if>
            <if cond="typeof(event$.values.phonenumber) != 'undefined'">
                <assign name="phonenumber" expr="event$.values.phonenumber"/>
            </if>
            <if cond="typeof(event$.values.action) != 'undefined'">
                <assign name="action" expr="event$.values.action"/>
            </if>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid conferenceid dialogid validated accesscode language deptcode dtmfpin calltype dialogreason action phonenumber"/>
        </transition>

        <transition event="dialog.terminatetransfer">
            <var name="eventname" expr="'DialogTransferTerminate'"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid conferenceid dialogid reason"/>
        </transition>

        <transition event="dialog.transfer">
            <var name="eventname" expr="'DialogTransfer'"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <var name="type" expr="event$.type"/>
            <var name="uri" expr="event$.URI"/>
            <var name="maxtime" expr="event$.maxtime"/>
            <var name="connecttimeout" expr="event$.connecttimeout"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname dialogid connectionid conferenceid type uri maxtime connecttimeout"/>
        </transition>

        <transition event="error.conference">
            <var name="eventname" expr="'ConferenceError'"/>
            <var name="conferenceid" expr="event$.conferenceid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname conferenceid reason"/>
        </transition>

        <transition event="error.conference.create">
            <var name="eventname" expr="'ConferenceErrorCreate'"/>
            <var name="conferenceid" expr="event$.conferenceid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname conferenceid reason"/>
        </transition>

        <transition event="error.conference.destroy">
            <var name="eventname" expr="'ConferenceErrorDestory'"/>
            <var name="conferenceid" expr="event$.conferenceid"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname conferenceid reason"/>
        </transition>

        <transition event="error.conference.join">
            <var name="eventname" expr="'ConferenceErrorJoin'"/>
            <var name="id1" expr="event$.id1"/>
            <var name="id2" expr="event$.id2"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname id1 id2 reason"/>
        </transition>

        <transition event="error.conference.unjoin">
            <var name="eventname" expr="'ConferenceErrorUnjoin'"/>
            <var name="id1" expr="event$.id1"/>
            <var name="id2" expr="event$.id2"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname id1 id2 reason"/>
        </transition>

        <transition event="error.connection">
            <var name="eventname" expr="'CallConnectionError'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason connectionLocal connectionRemote"/>
        </transition>

        <transition event="connection.merge.failed">
            <var name="eventname" expr="'CallConnectionMergeFailed'"/>
            <var name="connectionid1" expr="event$.id1"/>
            <var name="connectionid2" expr="event$.id2"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid1 connectionid2 reason"/>
        </transition>

        <transition event="error.connection.wrongstate">
            <var name="eventname" expr="'CallConnectionErrorWrongstate'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <var name="connectionLocal" expr="event$.connection.local"/>
            <var name="connectionRemote" expr="event$.connection.remote"/>
            <var name="tagname" expr="event$.tagname"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason connectionLocal connectionRemote tagname"/>
        </transition>

        <transition event="error.dialog">
            <var name="eventname" expr="'DialogError'"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname dialogid connectionid conferenceid reason"/>
        </transition>

        <transition event="error.dialog.notprepared">
            <var name="eventname" expr="'DialogErrorNotprepared'"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname dialogid connectionid conferenceid reason"/>
        </transition>

        <transition event="error.dialog.notstarted">
            <var name="eventname" expr="'DialogErrorNotstarted'"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname dialogid connectionid conferenceid reason"/>
        </transition>

        <transition event="dialog.user.*">
            <var name="eventname" expr="'DialogUser'"/>
            <var name="dialogid" expr="event$.dialogid"/>
            <var name="connectionid" expr="''"/>
            <if cond="typeof(event$.connectionid) != 'undefined'">
                <assign name="connectionid" expr="event$.connectionid"/>
            </if>
            <var name="conferenceid" expr="''"/>
            <if cond="typeof(event$.conferenceid) != 'undefined'">
                <assign name="conferenceid" expr="event$.conferenceid"/>
            </if>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname dialogid connectionid conferenceid"/>
        </transition>

        <transition event="error.notallowed">
            <var name="eventname" expr="'ErrorNotallowed'"/>
            <var name="connectionid" expr="event$.connectionid"/>
            <var name="reason" expr="event$.reason"/>
            <var name="tagname" expr="event$.tagname"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason tagname"/>
        </transition>

        <transition event="error.unsupported">
            <var name="eventname" expr="'ErrorUnsupported'"/>
            <var name="reason" expr="event$.reason"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname reason"/>
        </transition>

        <transition event="error.semantic">
            <var name="eventname" expr="'ErrorSemantic'"/>
            <var name="reason" expr="event$.reason"/>
            <var name="tagname" expr="event$.tagname"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname connectionid reason tagname"/>
        </transition>

        <transition event="error.send.*">
            <var name="eventname" expr="'ErrorSend'"/>
            <var name="reason" expr="event$.reason"/>
            <var name="tagname" expr="event$.tagname"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname reason tagname"/>
        </transition>

        <transition event="send.successful"/>

        <transition event="ccxml.loaded">
            <var name="eventname" expr="'CCXMLLoaded'"/>
            <var name="tempsessionid" expr="'<%=request.getParameter("tempsessionid")%>'"/>
            <send targettype="'basichttp'" target="targetAddr" name="event$" namelist="sessionid eventname tempsessionid"/>
        </transition>

    </eventprocessor>
</ccxml>
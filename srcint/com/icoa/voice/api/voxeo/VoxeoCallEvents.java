package com.icoa.voice.api.voxeo;

public enum VoxeoCallEvents {
	CallIncoming,
	CallOutgoing,
	CallConnected,
	CallDisconnected,
	CallConnectedError,
	CallConnectionError,
	CallConnectionFailed,
	CallConnectionErrorWrongstate,
	
	CallConnectionRejected,
	CallConnectionRejectFailed,
	
	CallConnectionMerged,
	CallConnectionMergeFailed,
	
	CallConnectionRedirected,
	CallConnectionRedirectFailed,
	
	ConferenceCreated,
	ConferenceDestroyed,
	ConferenceJoined,
	ConferenceUnjoined,
	ConferenceError,
	ConferenceErrorDestory,
	ConferenceErrorJoin,
	ConferenceErrorUnjoin,
	ConferenceErrorCreate,
	
	DialogStarted,
	DialogPrepared,
	DialogDisconnect,
	DialogExit,
	DialogError,
	DialogErrorNotprepared,
	DialogErrorNotstarted,
	DialogUser,
	
	DialogTransfer,
	DialogTransferTerminate,
	
	ErrorNotallowed,
	ErrorUnsupported,
	ErrorSemantic,
	ErrorSend,
	
	SessionDestroyed,
	
	Unknown
}
